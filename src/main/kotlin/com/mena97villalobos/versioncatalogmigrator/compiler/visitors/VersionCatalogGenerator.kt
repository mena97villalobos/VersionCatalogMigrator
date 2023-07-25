package com.mena97villalobos.versioncatalogmigrator.compiler.visitors

import com.mena97villalobos.versioncatalogmigrator.compiler.ast.Visitor
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.base.Terminal
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.declarations.*
import com.mena97villalobos.versioncatalogmigrator.compiler.ast.implementations.terminals.Identifier

class VersionCatalogGenerator: Visitor {

    data class Dependency(
        val group: List<Terminal>,
        var name: String,
        var version: String,
        var isVersionVariable: Boolean,
    )

    data class ModuleVersionIdentifier(
        val moduleIndex: Int,
        val groupIdentifiers: List<Terminal>,
        var moduleVersionName: String
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
        var moduleName = module.moduleName.visit(this, o) as String

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
                moduleName,
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
            val version = ModuleVersionIdentifier(index, dependency.group, dependency.name)
            var groupIndex = 0
            while (groupIndex < version.groupIdentifiers.size && finalVersionReferences.contains(version.moduleVersionName)) {
                version.moduleVersionName =
                    "${version.groupIdentifiers[groupIndex].visit(this, Unit)}-${version.moduleVersionName}"
                groupIndex++
            }
            var counter = 1
            while (finalVersionReferences.contains(version.moduleVersionName)) {
                version.moduleVersionName = "${version.moduleVersionName}-$counter"
                counter++
            }

            if(finalVersionsByVersion.contains(dependency.version)) {
                finalVersionsByVersion[dependency.version]?.add(version)
            } else {
                finalVersionsByVersion[dependency.version] = mutableListOf(version)
            }
            finalVersionReferences.add(version.moduleVersionName)
        }
        val regex = Regex("[^a-z]")
        finalVersionsByVersion.forEach { (key, value) ->
            val normalizedStrings = value.map {
                it.moduleVersionName = regex.replace(it.moduleVersionName, "")
                it
            }

            val matchingVersionName = getMaximumSharedSubstring(normalizedStrings.map { it.moduleVersionName })
            var versionNames = value
            if (matchingVersionName.isNotEmpty()) {
                versionNames = versionNames.map {
                    it.moduleVersionName = matchingVersionName
                    it
                }.toMutableList()
            }

            finalVersionsByVersion[key] = versionNames.map {
                it.moduleVersionName = "${it.moduleVersionName}-version"
                it
            }.toMutableList()
        }

        finalVersionCatalogue = finalVersionsByVersion
    }

    private fun generateVersions(): String {
        val versionsSection = arrayListOf<String>()
        finalVersionCatalogue.forEach { (versionNumber, dependenciesMap) ->
            dependenciesMap.distinctBy { it.moduleVersionName }.forEach {
                versionsSection.add("${it.moduleVersionName} = \"$versionNumber\"")
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
                    versionsSection.add("${it.moduleVersionName} = \"$versionNumber\"")
                    val groupReference = group.joinToString(".") { group -> group.visit(this@VersionCatalogGenerator, Unit) as String }
                    librariesSection.add("$name = { group = \"$groupReference\", name = \"$name\", version.ref = \"${it.moduleVersionName}\"}")

                    val versionCatalogReference = "libs.${name.replace("-", ".")}"
                    versionReference.add(VersionReference(versionCatalogReference, "$groupReference:$name:"))
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