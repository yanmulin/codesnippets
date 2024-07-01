package io.yanmulin.codesnippets.spring.aop.advices;

import io.yanmulin.codesnippets.spring.aop.target.DataSource;
import lombok.Setter;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.aop.target.HotSwappableTargetSource;

import java.io.IOException;

public class DataSourceThrowsAdvice implements ThrowsAdvice {

    boolean isPrimaryUsed = true;
    @Setter
    DataSource primary;
    @Setter
    DataSource secondary;
    @Setter
    HotSwappableTargetSource targetSource;

    public void afterThrowing(IOException exception) {
        if (isPrimaryUsed) {
            targetSource.swap(secondary);
            isPrimaryUsed = false;
            System.out.println("swapped to secondary data source");
        }
    }
}
