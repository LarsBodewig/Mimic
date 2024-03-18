package dev.bodewig.mimic.annotation.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import dev.bodewig.mimic.annotation.test.generated.MyTestClassMimic;
import dev.bodewig.mimic.annotation.test.generated.MySubClassMimic;

class MimicTest {

	@Test
	void getPublic() {
		MyTestClass orig = new MyTestClass();
		MyTestClassMimic mimic = new MyTestClassMimic(orig);
		assertEquals(1, mimic.getCount());
	}

	@Test
	void getPrivate() {
		MyTestClass orig = new MyTestClass();
		MyTestClassMimic mimic = new MyTestClassMimic(orig);
		assertEquals("test", mimic.getName());
	}

	@Test
	void setPublic() {
		MyTestClass orig = new MyTestClass();
		MyTestClassMimic mimic = new MyTestClassMimic(orig);
		mimic.setCount(2);
		assertEquals(2, mimic.getCount());
	}

	@Test
	void setPrivate() {
		MyTestClass orig = new MyTestClass();
		MyTestClassMimic mimic = new MyTestClassMimic(orig);
		mimic.setName("private");
		assertEquals("private", mimic.getName());
	}

	@Test
	void getInherited() {
		MySubClass sub = new MySubClass();
		MySubClassMimic mimic = new MySubClassMimic(sub);
		assertEquals(1, mimic.getCount());
	}

	@Test
	void setInherited() {
		MySubClass sub = new MySubClass();
		MySubClassMimic mimic = new MySubClassMimic(sub);
		mimic.setName("private");
		assertEquals("private", mimic.getName());
	}
}
