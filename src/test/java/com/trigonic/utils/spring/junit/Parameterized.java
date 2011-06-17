package com.trigonic.utils.spring.junit;

import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

/**
 * Similar to {@link org.junit.runners.Parameterized} but allows for readable labels and easier parameter specification.  A public
 * static method that returns a {@link LabelMaker} can be annotated with {@link LabelMakerFactory} and something other than the
 * {@link SimpleLabelMaker default labels} will be used.
 */
public class Parameterized extends Suite {
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LabelMakerFactory {
    }

    public interface LabelMaker {
        String getLabel(int index, Object[] parameters);

        String getTestName(int index, Object[] parameters, String methodName);
    }

    public static class SimpleLabelMaker implements LabelMaker {
        public String getLabel(int index, Object[] parameters) {
            return String.format("[%s]", index);
        }

        public String getTestName(int index, Object[] parameters, String methodName) {
            return String.format("%s[%s]", methodName, index);
        }
    }

    private class TestClassRunnerForParameters extends BlockJUnit4ClassRunner {
        private final int index;
        private final Object[] parameters;
        private final LabelMaker labelMaker;

        TestClassRunnerForParameters(Class<?> type, List<?> parameterList, int index, LabelMaker labelMaker)
            throws InitializationError {
            super(type);
            this.index = index;
            this.labelMaker = labelMaker;

            Object parameter = parameterList.get(index);
            if (parameter.getClass().isArray()) {
                parameters = (Object[]) parameter;
            } else {
                parameters = new Object[] { parameter };
            }

            if (labelMaker == null) {
                throw new NullPointerException("LabelMaker required");
            }

            // force IndexOutOfBoundsException if appropriate
            parameterList.get(index);
        }

        @Override
        public Object createTest() throws Exception {
            return getTestClass().getOnlyConstructor().newInstance(parameters);
        }

        @Override
        protected String getName() {
            return labelMaker.getLabel(index, parameters);
        }

        @Override
        protected String testName(final FrameworkMethod method) {
            return labelMaker.getTestName(index, parameters, method.getName());
        }

        @Override
        protected void validateConstructor(List<Throwable> errors) {
            validateOnlyOneConstructor(errors);
        }

        @Override
        protected Statement classBlock(RunNotifier notifier) {
            return childrenInvoker(notifier);
        }
    }

    private final ArrayList<Runner> runners = new ArrayList<Runner>();

    /**
     * This is only called reflexively, do not call directly.
     */
    public Parameterized(Class<?> testClass) throws Throwable {
        super(testClass, Collections.<Runner>emptyList());
        List<?> parametersList = getParametersList(getTestClass());
        assertTrue("No parameters found for " + testClass, parametersList.size() > 0);
        LabelMaker labelMaker = getLabelMaker(getTestClass());
        for (int i = 0; i < parametersList.size(); ++i) {
            runners.add(new TestClassRunnerForParameters(getTestClass().getJavaClass(), parametersList, i, labelMaker));
        }
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    private List<?> getParametersList(TestClass testClass) throws Throwable {
        Iterable<?> iterable = (Iterable<?>) getParametersMethod(testClass).invokeExplosively(null);
        if (iterable instanceof Collection<?>) {
            return new ArrayList<Object>((Collection<?>) iterable);
        }

        List<Object> arrayList = new ArrayList<Object>();
        for (Object each : iterable) {
            arrayList.add(each);
        }
        return arrayList;
    }

    private FrameworkMethod getParametersMethod(TestClass testClass) throws Exception {
        return getPublicStaticAnnotatedMethod(testClass, Parameters.class, true);
    }

    private LabelMaker getLabelMaker(TestClass testClass) throws Throwable {
        LabelMaker result;
        FrameworkMethod method = getLabelMakerFactoryMethod(testClass);
        if (method != null) {
            result = (LabelMaker) method.invokeExplosively(null);
        } else {
            result = new SimpleLabelMaker();
        }
        return result;
    }

    private FrameworkMethod getLabelMakerFactoryMethod(TestClass testClass) throws Exception {
        return getPublicStaticAnnotatedMethod(testClass, LabelMakerFactory.class, false);
    }

    private FrameworkMethod getPublicStaticAnnotatedMethod(TestClass testClass, Class<? extends Annotation> annotationType,
        boolean required) throws Exception {
        FrameworkMethod result = null;

        List<FrameworkMethod> methods = testClass.getAnnotatedMethods(annotationType);
        for (FrameworkMethod method : methods) {
            int modifiers = method.getMethod().getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
                result = method;
                break;
            }
        }

        if ((result == null) && required) {
            throw new Exception("No public static parameters @" + annotationType.getSimpleName() + " method on class " +
                testClass.getName());
        }

        return result;
    }
}
