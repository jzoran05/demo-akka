package sample.cluster.transformation

object TransformationApp {

    @JvmStatic
    fun main(args: Array<String>) {
        // starting 2 frontend nodes and 3 backend nodes
        TransformationBackendMain.main(arrayOf("2551"))
        TransformationBackendMain.main(arrayOf("2552"))
        TransformationBackendMain.main(arrayOf("0"))
        TransformationFrontendMain.main(arrayOf("0"))
    }
}
