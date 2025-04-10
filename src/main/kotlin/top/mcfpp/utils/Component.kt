package top.mcfpp.utils

import com.mojang.brigadier.Message

class Component(private val message:String): Message {
    override fun getString(): String{
        return message
    }

    companion object{
        @JvmStatic
        fun translatable(translatedKey:String):Component{
            return Component(translatedKey)
        }

        @JvmStatic
        fun translatableEscape(translatedKey: String, obj: Any?): Message {
            return Component(translatedKey+obj.toString())
        }
    }
}