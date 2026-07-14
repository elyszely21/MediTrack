package edu.cit.mabini.meditrack.appointment;

public class InvalidAppointmentRequestException extends IllegalArgumentException {
    public InvalidAppointmentRequestException(String message) {
        super(message);
    }
}

