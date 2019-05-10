package com.github.liebharc.queryenrichment

/**
 * A helper to draw a simple documentation for the attribute schema
 */
class SchemaDocumentation private constructor()// Singleton class
{

    fun drawSchema(attributes: Collection<Attribute<*>>): String {
        val result = StringBuilder()

        val byDomain = attributes.groupBy { attr -> attr.domain }
        byDomain.keys.stream().sorted().forEach { domain ->
            result.append(domain)
            result.append(":")
            result.append("\n")
            byDomain[domain]?.sortedBy { it.property }?.forEach { attr ->
                result.append("\t")
                result.append(" - ")
                result.append(attr.property)
                result.append(": ")
                result.append(attr.attributeClass.simpleName)
                result.append("\n")
            }
        }

        return result.toString()
    }

    companion object {

        /** Singleton  */
        val INSTANCE = SchemaDocumentation()
    }
}
