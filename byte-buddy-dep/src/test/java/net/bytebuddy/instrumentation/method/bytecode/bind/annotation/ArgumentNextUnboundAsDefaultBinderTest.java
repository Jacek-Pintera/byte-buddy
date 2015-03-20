package net.bytebuddy.instrumentation.method.bytecode.bind.annotation;

import net.bytebuddy.instrumentation.Instrumentation;
import net.bytebuddy.instrumentation.attribute.annotation.AnnotationDescription;
import net.bytebuddy.instrumentation.attribute.annotation.AnnotationList;
import net.bytebuddy.instrumentation.method.MethodDescription;
import net.bytebuddy.instrumentation.method.ParameterDescription;
import net.bytebuddy.instrumentation.method.ParameterList;
import net.bytebuddy.instrumentation.method.bytecode.stack.StackSize;
import net.bytebuddy.instrumentation.type.TypeDescription;
import net.bytebuddy.test.utility.MockitoRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class ArgumentNextUnboundAsDefaultBinderTest {

    @Rule
    public TestRule mockitoRule = new MockitoRule(this);

    @Mock
    private Instrumentation.Target instrumentationTarget;

    @Mock
    private MethodDescription source, target;

    @Mock
    private TypeDescription firstParameter, secondParameter;

    private ParameterList sourceParameters, targetParameters;

    @Mock
    private ParameterDescription firstTargetParameter, secondTargetParameter;

    @Before
    public void setUp() throws Exception {
        when(firstParameter.getStackSize()).thenReturn(StackSize.ZERO);
        when(secondParameter.getStackSize()).thenReturn(StackSize.ZERO);
        sourceParameters = ParameterList.Explicit.latent(source, Arrays.asList(firstParameter, secondParameter));
        targetParameters = new ParameterList.Explicit(Arrays.asList(firstTargetParameter, secondTargetParameter));
        when(source.getParameters()).thenReturn(sourceParameters);
        when(target.getParameters()).thenReturn(targetParameters);
    }

    @After
    public void tearDown() throws Exception {
        verifyZeroInteractions(instrumentationTarget);
    }

    @Test
    public void testFullyUnannotated() throws Exception {
        when(source.getParameters()).thenReturn(sourceParameters);
        when(firstTargetParameter.getDeclaredAnnotations()).thenReturn(new AnnotationList.Empty());
        when(secondTargetParameter.getDeclaredAnnotations()).thenReturn(new AnnotationList.Empty());
        Iterator<AnnotationDescription> iterator = Argument.NextUnboundAsDefaultsProvider.INSTANCE
                .makeIterator(instrumentationTarget, source, target);
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next().prepare(Argument.class).load().value(), is(0));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next().prepare(Argument.class).load().value(), is(1));
        assertThat(iterator.hasNext(), is(false));
        verify(source, atLeast(1)).getParameters();
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorRemoval() throws Exception {
        when(firstTargetParameter.getDeclaredAnnotations()).thenReturn(new AnnotationList.Empty());
        when(secondTargetParameter.getDeclaredAnnotations()).thenReturn(new AnnotationList.Empty());
        Iterator<AnnotationDescription> iterator = Argument.NextUnboundAsDefaultsProvider.INSTANCE
                .makeIterator(instrumentationTarget, source, target);
        assertThat(iterator.hasNext(), is(true));
        iterator.remove();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next().prepare(Argument.class).load().value(), is(1));
        assertThat(iterator.hasNext(), is(false));
        iterator.remove();
    }

    @Test
    public void testPartlyAnnotatedOrdered() throws Exception {
        Argument indexZeroArgument = mock(Argument.class);
        when(indexZeroArgument.value()).thenReturn(0);
        doReturn(Argument.class).when(indexZeroArgument).annotationType();
        when(firstTargetParameter.getDeclaredAnnotations()).thenReturn(new AnnotationList.ForLoadedAnnotation(indexZeroArgument));
        when(secondTargetParameter.getDeclaredAnnotations()).thenReturn(new AnnotationList.Empty());
        Iterator<AnnotationDescription> iterator = Argument.NextUnboundAsDefaultsProvider.INSTANCE
                .makeIterator(instrumentationTarget, source, target);
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next().prepare(Argument.class).load().value(), is(1));
        assertThat(iterator.hasNext(), is(false));
        verify(source, atLeast(1)).getParameters();
    }

    @Test
    public void testPartlyAnnotatedUnordered() throws Exception {
        Argument indexOneArgument = mock(Argument.class);
        when(indexOneArgument.value()).thenReturn(1);
        doReturn(Argument.class).when(indexOneArgument).annotationType();
        when(firstTargetParameter.getDeclaredAnnotations()).thenReturn(new AnnotationList.ForLoadedAnnotation(indexOneArgument));
        when(secondTargetParameter.getDeclaredAnnotations()).thenReturn(new AnnotationList.Empty());
        Iterator<AnnotationDescription> iterator = Argument.NextUnboundAsDefaultsProvider.INSTANCE
                .makeIterator(instrumentationTarget, source, target);
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next().prepare(Argument.class).load().value(), is(0));
        assertThat(iterator.hasNext(), is(false));
        verify(source, atLeast(1)).getParameters();
    }

    @Test
    public void testFullyAnnotatedUnordered() throws Exception {
        Argument indexZeroArgument = mock(Argument.class);
        when(indexZeroArgument.value()).thenReturn(0);
        doReturn(Argument.class).when(indexZeroArgument).annotationType();
        Argument indexOneArgument = mock(Argument.class);
        when(indexOneArgument.value()).thenReturn(1);
        doReturn(Argument.class).when(indexOneArgument).annotationType();
        when(firstTargetParameter.getDeclaredAnnotations()).thenReturn(new AnnotationList.ForLoadedAnnotation(indexOneArgument));
        when(secondTargetParameter.getDeclaredAnnotations()).thenReturn(new AnnotationList.ForLoadedAnnotation(indexZeroArgument));
        Iterator<AnnotationDescription> iterator = Argument.NextUnboundAsDefaultsProvider.INSTANCE
                .makeIterator(instrumentationTarget, source, target);
        assertThat(iterator.hasNext(), is(false));
        verify(source, atLeast(1)).getParameters();
    }
}