package androidx.compose.ui.tooling.preview

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class Preview(
    val name: String = "",
    val group: String = "",
    val showBackground: Boolean = false,
    val backgroundColor: Long = 0,
    val showSystemUi: Boolean = false,
    val device: String = "",
    val widthDp: Int = -1,
    val heightDp: Int = -1,
    val locale: String = "",
    val uiMode: Int = 0,
    val fontScale: Float = 1f
)

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class PreviewParameter(val provider: kotlin.reflect.KClass<*> = Unit::class, val limit: Int = Int.MAX_VALUE)

interface PreviewParameterProvider<T> {
    val values: Sequence<T>
    val count: Int get() = values.count()
    fun getDisplayName(index: Int): String = "Item " + index
}