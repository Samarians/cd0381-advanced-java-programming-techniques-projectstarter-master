package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {
  private final Clock clock;
  private final Object delegate;
  private final ProfilingState state;
  // TODO: You will need to add more instance fields and constructor arguments to this class.
  ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState state) {
    this.clock = Objects.requireNonNull(clock);
    this.delegate = delegate;
    this.state = state;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // TODO: This method interceptor should inspect the called method to see if it is a profiled
    //       method. For profiled methods, the interceptor should record the start time, then
    //       invoke the method using the object that is being profiled. Finally, for profiled
    //       methods, the interceptor should record how long the method call took, using the
    //       ProfilingState methods.
    boolean isProfiled = method.isAnnotationPresent(Profiled.class);
    Instant start = null;

    if (isProfiled) {
      start = clock.instant();
    }

    try {
      // Invoke the method on the delegate object
      Object result = method.invoke(delegate, args);

      if (isProfiled) {
        Instant end = clock.instant();
        state.record(delegate.getClass(), method, Duration.between(start, end));
      }

      return result;
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Failed to access method: " + method.getName(), e);
    } catch (InvocationTargetException e) {
      if (isProfiled) {
        Instant end = clock.instant();
        state.record(delegate.getClass(), method, Duration.between(start, end));
      }
      throw e.getCause();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    ProfilingMethodInterceptor that = (ProfilingMethodInterceptor) obj;
    return Objects.equals(clock, that.clock) &&
            Objects.equals(delegate, that.delegate) &&
            Objects.equals(state, that.state);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clock, delegate, state);
  }

  @Override
  public String toString() {
    return "ProfilingMethodInterceptor{" +
            "clock=" + clock +
            ", delegate=" + delegate +
            ", state=" + state +
            '}';
  }
}