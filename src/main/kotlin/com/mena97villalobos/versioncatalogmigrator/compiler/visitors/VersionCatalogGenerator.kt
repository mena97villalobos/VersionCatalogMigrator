package com.mena97villalobos.versioncatalogmigrator.compiler.visitors

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.Visitor
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.Terminal
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.declarations.*
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals.Identifier

class VersionCatalogGenerator: Visitor {

    data class Dependency(
        val group: List<Terminal>,
        var originalName: String,
        var catalogName: String,
        var version: String,
        var isVersionVariable: Boolean,
    )

    data class ModuleVersionIdentifier(
        val moduleIndex: Int,
        val groupIdentifiers: List<Terminal>,
        val originalName: String,
        var catalogName: String
    )

    private val variables = hashMapOf<String, String>()
    private val modules = arrayListOf<Dependency>()
    private var dependencyBlockVisited = false
    private val moduleNamesDistinct = arrayListOf<String>()

    val versionReference = mutableListOf<VersionReference>()

    private lateinit var finalVersionCatalogue: HashMap<String, MutableList<ModuleVersionIdentifier>>

    override fun visitIdentifier(declaration: Identifier, o: Any): String {
        return declaration.spelling
    }

    override fun visitImplementationDeclaration(declaration: DependencyImplementationDeclaration, o: Any): Any {
        return declaration.dependencyIdentifier.visit(this, o)
    }

    override fun visitUnrelatedFileContent(content: UnrelatedFileContent, o: Any): Any {
        // Do nothing with unrelated content
        return Unit
    }

    override fun visitVariableDeclaration(variable: VariableDeclaration, o: Any): Any {
        variables[variable.identifier.visit(this, o) as String] = variable.value.visit(this, o) as String
        return variables
    }

    override fun visitDependencyBlock(block: DependenciesBlockDeclaration, o: Any): Any {
        block.implementations.forEach {
            it.visit(this, o)
        }
        block.variables.forEach {
            it.visit(this, o)
        }
        dependencyBlockVisited = true
        return block
    }

    override fun visitModuleIdentifier(module: DependencyDeclaration, o: Any): Any {
        val moduleNameValue = module.versionName.visit(this, o) as String
        val originalName = module.moduleName.visit(this, o) as String
        var moduleName = originalName

        var groupIndex = 0
        while (groupIndex < module.identifiers.size && moduleNamesDistinct.contains(moduleName)) {
            moduleName = "${module.identifiers[groupIndex].visit(this, Unit)}-${moduleName}"
            groupIndex++
        }

        var counter = 1
        while (moduleNamesDistinct.contains(moduleName)) {
            moduleName = "${module.identifiers[groupIndex].visit(this, Unit)}-${moduleName}"
            counter++
        }
        moduleNamesDistinct.add(moduleName)

        modules.add(
            Dependency(
                module.identifiers,
                originalName = originalName,
                catalogName = moduleName,
                moduleNameValue,
                moduleNameValue.startsWith("$")
            )
        )
        return modules
    }

    fun generateVersionCatalogue(): String {
        if (dependencyBlockVisited) {
            mapVariableNamesToActualNumber()
            reviewAndAdaptVersions()
            return "${generateVersions()}${generateModules()}"
        }
        return ""
    }

    private fun mapVariableNamesToActualNumber() {
        modules.filter { it.isVersionVariable }
            .forEach {
                val versionKey = it.version.removePrefix("$")
                val actualVersion = variables.getOrDefault(versionKey, it.version)
                it.version = actualVersion
                it.isVersionVariable = false
            }
    }

    private fun reviewAndAdaptVersions() {
        val finalVersionsByVersion = hashMapOf<String, MutableList<ModuleVersionIdentifier>>()
        val finalVersionReferences = arrayListOf<String>()
        modules.forEachIndexed { index, dependency ->
            val version = ModuleVersionIdentifier(
                index,
                dependency.group,
                originalName = dependency.originalName,
                catalogName = dependency.catalogName
            )
            var groupIndex = 0
            while (groupIndex < version.groupIdentifiers.size && finalVersionReferences.contains(version.catalogName)) {
                version.catalogName =
                    "${version.groupIdentifiers[groupIndex].visit(this, Unit)}-${version.catalogName}"
                groupIndex++
            }
            var counter = 1
            while (finalVersionReferences.contains(version.catalogName)) {
                version.catalogName = "${version.catalogName}-$counter"
                counter++
            }

            if(finalVersionsByVersion.contains(dependency.version)) {
                finalVersionsByVersion[dependency.version]?.add(version)
            } else {
                finalVersionsByVersion[dependency.version] = mutableListOf(version)
            }
            finalVersionReferences.add(version.catalogName)
        }
        val regex = Regex("[^a-z]")
        finalVersionsByVersion.forEach { (key, value) ->
            val normalizedStrings = value.map {
                it.catalogName = regex.replace(it.catalogName, "")
                it
            }

            val matchingVersionName = getMaximumSharedSubstring(normalizedStrings.map { it.catalogName })
            var versionNames = value
            if (matchingVersionName.isNotEmpty()) {
                versionNames = versionNames.map {
                    it.catalogName = matchingVersionName
                    it
                }.toMutableList()
            }

            finalVersionsByVersion[key] = versionNames.map {
                it.catalogName = "${it.catalogName}-version"
                it
            }.toMutableList()
        }

        finalVersionCatalogue = finalVersionsByVersion
    }

    private fun generateVersions(): String {
        val versionsSection = arrayListOf<String>()
        finalVersionCatalogue.forEach { (versionNumber, dependenciesMap) ->
            dependenciesMap.distinctBy { it.catalogName }.forEach {
                versionsSection.add("${it.catalogName} = \"$versionNumber\"")
            }
        }
        return "[versions]\n${versionsSection.joinToString("\n")}\n\n"
    }

    private fun generateModules(): String {
        val librariesSection = arrayListOf<String>()
        val versionsSection = arrayListOf<String>()

        finalVersionCatalogue.forEach { (versionNumber, dependenciesMap) ->
            dependenciesMap.forEach {
                with(modules[it.moduleIndex]) {
                    versionsSection.add("${it.catalogName} = \"$versionNumber\"")
                    val groupReference = group.joinToString(".") { group -> group.visit(this@VersionCatalogGenerator, Unit) as String }
                    librariesSection.add("$catalogName = { group = \"$groupReference\", name = \"$originalName\", version.ref = \"${it.catalogName}\"}")

                    val versionCatalogReference = "libs.${catalogName.replace("-", ".")}"
                    versionReference.add(VersionReference(versionCatalogReference, "$groupReference:$originalName:"))
                }
            }
        }

        return "[libraries]\n${librariesSection.joinToString("\n")}\n\n"
    }

    private fun getMaximumSharedSubstring(strings: List<String>): String {
        val minNameLength = strings.minOfOrNull { it.length - 1 } ?: 0
        val stringBuilder = StringBuilder()

        var matchingChar: Char?
        var index = 0
        do {
            matchingChar = strings.map { name -> name[index] }.distinct().takeIf { it.size == 1 }?.get(0)
            if (matchingChar != null) {
                stringBuilder.append(matchingChar)
            }
            index++
        } while (matchingChar != null && index <= minNameLength)
        return stringBuilder.toString()
    }
}

data class VersionReference(
    val libsReference: String,
    val originalReference: String
)