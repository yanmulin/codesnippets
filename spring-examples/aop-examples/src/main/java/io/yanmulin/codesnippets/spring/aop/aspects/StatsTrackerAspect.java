package io.yanmulin.codesnippets.spring.aop.aspects;

import io.yanmulin.codesnippets.spring.aop.target.DefaultStatsTracker;
import io.yanmulin.codesnippets.spring.aop.target.IStatsTracker;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;

@Aspect
public class StatsTrackerAspect {
    @DeclareParents(
            value = "io.yanmulin.codesnippets.spring.aop.target.TargetBean",
            defaultImpl = DefaultStatsTracker.class
    )
    public IStatsTracker mixin;
}
