package com.prottasha.circle.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class PodRepositoryTest {
    @Test
    public void showcasePodIdIsFixedAndNotUserControlled() {
        assertEquals("academic-showcase", PodRepository.SHOWCASE_POD_ID);
    }
}
