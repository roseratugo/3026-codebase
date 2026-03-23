package fr.mma.df.codinggame.api.core.enums

enum class ResourceTypeEnum {
    CHARBONIUM, FERONIUM, BOISIUM;

    companion object {
        private var index = 0;

        fun roundRobin(): ResourceTypeEnum {
            val values = entries.toTypedArray()
            val resource = values[index % values.size]
            index = (index + 1) % values.size
            return resource
        }
    }
}
