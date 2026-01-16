package com.anjunar.technologyspeaks.shared.editor

import com.anjunar.json.mapper.ObjectMapperProvider
import org.hibernate.type.descriptor.WrapperOptions
import org.hibernate.usertype.UserType
import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.util.Objects

class NodeType : UserType<Node> {

    private val objectMapper = ObjectMapperProvider.mapper

    override fun getSqlType(): Int = Types.OTHER

    override fun returnedClass(): Class<Node> = Node::class.java

    override fun equals(x: Node?, y: Node?): Boolean = Objects.equals(x, y)

    override fun hashCode(x: Node?): Int = Objects.hashCode(x)

    override fun nullSafeGet(
        rs: ResultSet,
        position: Int,
        options: WrapperOptions
    ): Node? {
        val json = rs.getString(position)
        return if (json == null) null else objectMapper.readValue(json, Node::class.java)
    }

    override fun nullSafeSet(
        st: PreparedStatement,
        value: Node?,
        position: Int,
        options: WrapperOptions
    ) {
        if (value == null) {
            st.setNull(position, Types.OTHER)
        } else {
            val json = objectMapper.writeValueAsString(value)
            st.setObject(position, json, Types.OTHER)
        }
    }

    override fun deepCopy(value: Node?): Node? {
        if (value == null) return null
        val json = objectMapper.writeValueAsString(value)
        return objectMapper.readValue(json, Node::class.java)
    }

    override fun isMutable(): Boolean = true

    override fun disassemble(value: Node?): Serializable {
        // Hibernate erwartet hier ein serialisierbares Cache-Objekt
        return deepCopy(value) as Serializable
    }

    override fun assemble(cached: Serializable?, owner: Any?): Node? {
        return deepCopy(cached as? Node)
    }
}
