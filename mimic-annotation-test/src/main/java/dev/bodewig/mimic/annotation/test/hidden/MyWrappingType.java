package dev.bodewig.mimic.annotation.test.hidden;

import dev.bodewig.mimic.annotation.Mimic;

@SuppressWarnings("unused")
@Mimic(packageName = "dev.bodewig.mimic.annotation.test.generated")
public class MyWrappingType {
    private MyHiddenType hidden = new MyHiddenType("one");
}
