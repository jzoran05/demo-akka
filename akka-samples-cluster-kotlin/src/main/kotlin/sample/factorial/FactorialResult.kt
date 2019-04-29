package sample.cluster.factorial

import java.io.Serializable
import java.math.BigInteger

class FactorialResult internal constructor(val n: Int, val factorial: BigInteger) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
