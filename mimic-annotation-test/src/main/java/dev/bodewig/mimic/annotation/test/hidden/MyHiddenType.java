package dev.bodewig.mimic.annotation.test.hidden;

@SuppressWarnings("unused")
class MyHiddenType {
    public String secret;

    MyHiddenType(String secret) {
        this.secret = secret;
    }
}
