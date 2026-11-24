package com.candidate.appointment;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {
    private static final String S="2026-11-24T10:00:00Z", E="2026-11-24T11:00:00Z";

    @Test void successfulBooking() {
        BookingService s=new BookingService();
        BookingResult r=s.book("a1","p1",S,E);
        assertTrue(r.success()); assertEquals(AppointmentStatus.ACTIVE,r.appointment().status());
    }
    @Test void sameProviderOverlapRejected() {
        BookingService s=new BookingService(); assertTrue(s.book("a1","p1",S,E).success());
        BookingResult r=s.book("a2","p1","2026-11-24T10:30:00Z","2026-11-24T11:30:00Z");
        assertFalse(r.success()); assertEquals(1,s.findAll().size());
    }
    @Test void adjacentAppointmentsAllowed() {
        BookingService s=new BookingService(); s.book("a1","p1",S,E);
        assertTrue(s.book("a2","p1",E,"2026-11-24T12:00:00Z").success());
    }
    @Test void differentProvidersDoNotConflict() {
        BookingService s=new BookingService(); s.book("a1","p1",S,E);
        assertTrue(s.book("a2","p2",S,E).success());
    }
    @Test void cancellationReleasesSlot() {
        BookingService s=new BookingService(); s.book("a1","p1",S,E);
        assertTrue(s.cancel("a1").success());
        assertEquals(AppointmentStatus.CANCELLED,s.findById("a1").status());
        assertTrue(s.book("a2","p1",S,E).success());
    }
    @Test void missingAndRepeatedCancellationAreClear() {
        BookingService s=new BookingService();
        assertFalse(s.cancel("missing").success());
        s.book("a1","p1",S,E); assertTrue(s.cancel("a1").success());
        assertFalse(s.cancel("a1").success());
    }
    @Test void rejectsMissingIdentifiersWithoutMutation() {
        BookingService s=new BookingService();
        assertThrows(BookingValidationException.class,()->s.book(null,"p1",S,E));
        assertThrows(BookingValidationException.class,()->s.book("a1"," ",S,E));
        assertThrows(BookingValidationException.class,()->s.cancel(" "));
        assertTrue(s.findAll().isEmpty());
    }
    @Test void rejectsMalformedTimestampWithoutMutation() {
        BookingService s=new BookingService();
        assertThrows(BookingValidationException.class,()->s.book("a1","p1","bad",E));
        assertTrue(s.findAll().isEmpty());
    }
    @Test void rejectsTimestampWithoutTimezone() {
        BookingService s=new BookingService();
        assertThrows(BookingValidationException.class,()->s.book("a1","p1","2026-11-24T10:00:00",E));
        assertTrue(s.findAll().isEmpty());
    }
    @Test void rejectsNonUtcOffset() {
        BookingService s=new BookingService();
        assertThrows(BookingValidationException.class,()->s.book("a1","p1","2026-11-24T10:00:00+05:30","2026-11-24T11:00:00+05:30"));
        assertTrue(s.findAll().isEmpty());
    }
    @Test void rejectsZeroDuration() {
        BookingService s=new BookingService();
        assertThrows(BookingValidationException.class,()->s.book("a1","p1",S,S));
        assertTrue(s.findAll().isEmpty());
    }
    @Test void rejectsNegativeDuration() {
        BookingService s=new BookingService();
        assertThrows(BookingValidationException.class,()->s.book("a1","p1",E,S));
        assertTrue(s.findAll().isEmpty());
    }
    @Test void rejectsDuplicateIdWithoutOverwrite() {
        BookingService s=new BookingService(); s.book("a1","p1",S,E);
        assertThrows(BookingValidationException.class,()->s.book("a1","p2","2026-11-24T12:00:00Z","2026-11-24T13:00:00Z"));
        assertEquals("p1",s.findById("a1").providerId());
    }
    @Test void simultaneousOverlappingBookingsOnlyOneWins() throws Exception {
        BookingService s=new BookingService();
        CountDownLatch ready=new CountDownLatch(2), go=new CountDownLatch(1);
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Future<BookingResult> f1=pool.submit(()->{ready.countDown();go.await();return s.book("x1","p1",S,E);});
            Future<BookingResult> f2=pool.submit(()->{ready.countDown();go.await();return s.book("x2","p1",S,E);});
            ready.await(); go.countDown();
            int wins=(f1.get().success()?1:0)+(f2.get().success()?1:0);
            assertEquals(1,wins); assertEquals(1,s.findAll().size());
        } finally { pool.shutdownNow(); }
    }
}
