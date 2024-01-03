package dev.bodewig.mimic.maven.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import dev.bodewig.mimic.maven.test.generated.MyTestClassMimic;

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
}