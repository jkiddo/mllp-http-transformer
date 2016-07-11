package test;

import java.util.Collections;
import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class RandomBlockJUnit4ClassRunner extends BlockJUnit4ClassRunner {

	public RandomBlockJUnit4ClassRunner(Class<?> clazz) throws InitializationError {
		super(clazz);
	}

	protected java.util.List<org.junit.runners.model.FrameworkMethod> computeTestMethods() {
		List<FrameworkMethod> scrambledOrder = Lists.newArrayList(super.computeTestMethods());
		Collections.shuffle(scrambledOrder);
		return ImmutableList.copyOf(scrambledOrder);
	}

}