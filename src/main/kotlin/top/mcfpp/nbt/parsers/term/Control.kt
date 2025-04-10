package top.mcfpp.nbt.parsers.term

interface Control {
    fun cut()

    fun hasCut(): Boolean

    companion object {
        val UNBOUND: Control = object : Control {
            override fun cut() {
            }

            override fun hasCut(): Boolean {
                return false
            }
        }
    }
}

class SimpleControl : Control {
    private var hasCut = false

    override fun cut() {
        this.hasCut = true
    }

    override fun hasCut(): Boolean {
        return this.hasCut
    }

    fun reset() {
        this.hasCut = false
    }
}
