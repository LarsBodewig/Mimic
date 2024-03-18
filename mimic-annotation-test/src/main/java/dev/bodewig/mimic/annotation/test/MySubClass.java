package dev.bodewig.mimic.annotation.test;

import dev.bodewig.mimic.annotation.Mimic;

@SuppressWarnings("unused")
@Mimic(packageName = "dev.bodewig.mimic.annotation.test.generated")
public class MySubClass extends MyTestClass {

	protected long id = 1;
}
