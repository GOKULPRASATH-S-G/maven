package com.hcl.petadoption.service;

import com.hcl.petadoption.model.HomeVisit;
import com.hcl.petadoption.model.HomeVisitStatus;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

/**
 * Service managing home-visit scheduling using a {@link Queue}.
 *
 * <p>Home visits are queued in FIFO order – the first visit scheduled is the first
 * to be processed (like a real appointment queue at a shelter).
 *
 * <p>Demonstrates:
 * <ul>
 *   <li><b>Queue collection</b>  – {@link LinkedList} used as a FIFO queue</li>
 *   <li><b>Encapsulation</b>     – queue is never exposed directly</li>
 *   <li><b>java.time</b>         – {@link LocalDateTime} for scheduled date/time</li>
 *   <li><b>Optional</b>          – {@code peekNextVisit()} may return empty</li>
 * </ul>
 */
public class HomeVisitService {

    /**
     * Queue of pending home visits.  Uses {@link LinkedList} which implements the
     * {@link Queue} interface providing offer/poll/peek semantics.
     *
     * <p>Demonstrates: <b>Queue</b> collection.
     */
    private final Queue<HomeVisit> pendingVisits = new LinkedList<>();

    /** All visits ever created, kept for audit/reporting (not just pending ones). */
    private final List<HomeVisit> allVisits = new LinkedList<>();

    // -------------------------------------------------------------------------
    // Scheduling (enqueue)
    // -------------------------------------------------------------------------

    /**
     * Creates a new home visit and enqueues it.
     * Demonstrates: Queue.offer (enqueue).
     *
     * @param applicationId  the approved application this visit is tied to
     * @param scheduledDateTime when the visit is scheduled
     * @return the newly scheduled {@link HomeVisit}
     */
    public HomeVisit scheduleVisit(String applicationId, LocalDateTime scheduledDateTime) {
        String visitId = "VIS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        HomeVisit visit = new HomeVisit(visitId, applicationId, scheduledDateTime);
        pendingVisits.offer(visit);   // enqueue
        allVisits.add(visit);
        return visit;
    }

    // -------------------------------------------------------------------------
    // Queue inspection
    // -------------------------------------------------------------------------

    /**
     * Returns the next pending visit without removing it.
     * Demonstrates: Queue.peek.
     */
    public Optional<HomeVisit> peekNextVisit() {
        return Optional.ofNullable(pendingVisits.peek());
    }

    /**
     * Returns the number of pending visits in the queue.
     */
    public int pendingCount() {
        return pendingVisits.size();
    }

    // -------------------------------------------------------------------------
    // Processing (dequeue)
    // -------------------------------------------------------------------------

    /**
     * Processes (dequeues) the next pending home visit and marks it COMPLETED.
     * Demonstrates: Queue.poll (dequeue).
     *
     * @param staffNotes notes recorded by the shelter staff
     * @return the completed {@link HomeVisit}, or {@link Optional#empty()} if queue is empty
     */
    public Optional<HomeVisit> processNextVisit(String staffNotes) {
        HomeVisit visit = pendingVisits.poll();   // dequeue
        if (visit == null) {
            return Optional.empty();
        }
        visit.setStatus(HomeVisitStatus.COMPLETED);
        visit.setStaffNotes(staffNotes != null ? staffNotes : "");
        return Optional.of(visit);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns a snapshot of all pending visits (unmodifiable).
     */
    public List<HomeVisit> getPendingVisits() {
        return Collections.unmodifiableList(new LinkedList<>(pendingVisits));
    }

    /**
     * Returns all visits ever scheduled (unmodifiable).
     */
    public List<HomeVisit> getAllVisits() {
        return Collections.unmodifiableList(allVisits);
    }
}
