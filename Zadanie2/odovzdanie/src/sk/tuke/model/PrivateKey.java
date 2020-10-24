package sk.tuke.model;

import java.math.BigInteger;

public class PrivateKey {
    private BigInteger d;
    private BigInteger modulus;

    public PrivateKey(BigInteger d, BigInteger modulus) {
        this.d = d;
        this.modulus = modulus;
    }

    public BigInteger getD() {
        return d;
    }

    public BigInteger getModulus() {
        return modulus;
    }
}
