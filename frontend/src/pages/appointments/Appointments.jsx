import { useEffect, useState, useCallback } from "react";
import axios from "../../shared/api/axios";
import Layout from "../../widgets/layout/Layout";

const STATUS_CONFIG = {
  REQUESTED:            { bg: "bg-yellow-100", text: "text-yellow-800", label: "Requested" },
  PENDING_APPROVAL:     { bg: "bg-blue-100",   text: "text-blue-800",   label: "Pending approval" },
  APPROVED:             { bg: "bg-green-100",  text: "text-green-800",  label: "Approved" },
  CHECKED_IN:          { bg: "bg-indigo-100", text: "text-indigo-800", label: "Checked-in" },
  WAITING:             { bg: "bg-purple-100", text: "text-purple-800", label: "Waiting" },
  IN_CONSULTATION:     { bg: "bg-cyan-100",   text: "text-cyan-800",   label: "In consultation" },
  PRESCRIPTION_ISSUED: { bg: "bg-teal-100",   text: "text-teal-800",   label: "Prescription issued" },
  COMPLETED:           { bg: "bg-green-100",  text: "text-green-800",  label: "Completed" },
  NO_SHOW:             { bg: "bg-gray-100",   text: "text-gray-600",   label: "No-show" },
  CANCELLED:           { bg: "bg-gray-100",   text: "text-gray-600",   label: "Cancelled" },
  REJECTED:            { bg: "bg-red-100",    text: "text-red-700",    label: "Rejected" },
};

const STATUS_FILTERS = [
  "ALL",
  "REQUESTED",
  "PENDING_APPROVAL",
  "APPROVED",
  "CHECKED_IN",
  "WAITING",
  "IN_CONSULTATION",
  "PRESCRIPTION_ISSUED",
  "COMPLETED",
  "NO_SHOW",
  "CANCELLED",
  "REJECTED",
];


export default function Appointments() {
  const user    = JSON.parse(localStorage.getItem("meditrackUser") || "{}");
  const role     = user?.role;
  const isAdmin  = role === "SUPER_ADMIN";
  const isStaff  = role === "SUPER_ADMIN" || role === "DOCTOR" || role === "NURSE";

  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading]           = useState(true);
  const [error, setError]               = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [showForm, setShowForm]         = useState(false);
  const [actionTarget, setActionTarget] = useState(null);
  const [actionType, setActionType]     = useState("");
  const [reason, setReason]             = useState("");
  const [submitting, setSubmitting]     = useState(false);
  const [successMsg, setSuccessMsg]     = useState("");

  // Edit A — form state
  const [form, setForm] = useState({
    patientNumber: "",
    doctorId: "",
    appointmentDate: "",
    appointmentTime: "",
    remarks: ""
  });

  // Patient mode: only PATIENT role sees the read-only history view.
  const isPatient = role === "PATIENT";


  const [formError, setFormError] = useState("");
  const [, setPatientId] = useState(null);

  // Doctor dropdown (frontend-only; backend already supports doctorId in AppointmentDto)
  const [doctors, setDoctors] = useState([]);
  const [doctorsLoading, setDoctorsLoading] = useState(false);
  const [doctorsError, setDoctorsError] = useState("");

  const fetchDoctors = useCallback(async () => {
    setDoctorsLoading(true);
    setDoctorsError("");
    try {
      const res = await axios.get("/doctors/schedule");
      const list = Array.isArray(res.data) ? res.data : (res.data?.content || []);
      setDoctors(list);
    } catch (e) {
      setDoctorsError(e?.response?.data?.message || "Failed to load doctors.");
    } finally {
      setDoctorsLoading(false);
    }
  }, []);



  // ── Fetch ──────────────────────────────────────────────────────────────────


  const fetchAppointments = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      let res;
      if (role === "DOCTOR") {
        const doctorRes = await axios.get("/appointments/doctor/me");
        let list = doctorRes.data || [];
        if (statusFilter !== "ALL") {
          list = list.filter(a => a.status === statusFilter);
        }
        res = { data: list };
      } else {
        res = statusFilter === "ALL"
          ? await axios.get("/appointments")
          : await axios.get(`/appointments/status/${statusFilter}`);
      }
      setAppointments(res.data);
    } catch {
      setError("Failed to load appointments.");
    } finally {
      setLoading(false);
    }
  }, [statusFilter, role]);

  useEffect(() => { fetchAppointments(); }, [fetchAppointments]);
  useEffect(() => { fetchDoctors(); }, [fetchDoctors]);


  const showSuccess = (msg) => {
    setSuccessMsg(msg);
    setTimeout(() => setSuccessMsg(""), 3000);
  };

  const actionLabel = (type) => {
    switch (type) {
      case "approve":            return "Approve this appointment";
      case "check-in":           return "Check in this appointment";
      case "waiting":            return "Move this appointment to waiting";
      case "in-consultation":    return "Start consultation for this appointment";
      case "prescription-issued":return "Mark prescription issued for this appointment";
      case "complete":           return "Mark this appointment as completed";
      case "no-show":            return "Mark this appointment as no-show";
      default:                   return "Update this appointment";
    }
  };

  // ── Create ─────────────────────────────────────────────────────────────────

  // Edit B — handleSubmit
  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError("");

    if (!form.patientNumber || !form.appointmentDate || !form.appointmentTime) {
      setFormError("Patient number, date and time are required.");
      return;
    }

    if (!form.doctorId) {
      setFormError("Doctor is required.");
      return;
    }

    setSubmitting(true);
    try {
      // Fix async lookup bug: use resolved ID directly (do not rely on React state updates).
      const lookupRes = await axios.get(
        `/appointments/lookup-patient/${encodeURIComponent(form.patientNumber.trim())}`
      );

      const resolvedPatientId = lookupRes?.data?.id;
      if (!resolvedPatientId) {
        setFormError(`No patient found with number '${form.patientNumber}'.`);
        return;
      }

      const durationMinutes = 30;
      const [year, month, day] = form.appointmentDate.split('-').map(Number);
      const [hours, minutes] = form.appointmentTime.split(':').map(Number);
      const startDate = new Date(year, month - 1, day, hours, minutes);
      const endDate = new Date(startDate.getTime() + durationMinutes * 60 * 1000);
      const endTime = endDate.toTimeString().slice(0, 5);

      await axios.post("/appointments", {
        patientId: resolvedPatientId,
        doctorId: form.doctorId,
        appointmentType: "CONSULTATION",
        durationMinutes: durationMinutes,
        appointmentDate: form.appointmentDate,
        appointmentTime: form.appointmentTime + ":00",
        endTime: endTime + ":00",
        remarks: form.remarks || "—"
      });

      setShowForm(false);
      setForm({ patientNumber:"", appointmentDate:"", appointmentTime:"", doctorId:"", remarks:"" });
      setPatientId(null);
      showSuccess("Appointment scheduled successfully.");
      fetchAppointments();
    } catch (e) {
      if (e.response?.status === 404) {
        setFormError(`No patient found with number '${form.patientNumber}'.`);
        return;
      }
      setFormError(e.response?.data?.message || "Something went wrong.");
    } finally {
      setSubmitting(false);
    }
  };


  // ── Approval actions ───────────────────────────────────────────────────────

  const openAction = (appointment, type) => {
    setActionTarget(appointment);
    setActionType(type);
    setReason("");
  };

  const handleAction = async () => {
    if (!actionTarget) return;
    setSubmitting(true);
    try {
      const id = actionTarget.id;
      switch (actionType) {
        case "approve":
          await axios.put(`/appointments/${id}/approve`);
          showSuccess("Appointment approved.");
          break;
        case "reject":
          await axios.put(`/appointments/${id}/reject`,
            { reason: reason || null });
          showSuccess("Appointment rejected.");
          break;
        case "check-in":
          await axios.put(`/appointments/${id}/check-in`);
          showSuccess("Appointment checked-in.");
          break;
        case "waiting":
          await axios.put(`/appointments/${id}/waiting`);
          showSuccess("Appointment moved to waiting.");
          break;
        case "in-consultation":
          await axios.put(`/appointments/${id}/in-consultation`);
          showSuccess("Appointment in consultation.");
          break;
        case "prescription-issued":
          await axios.put(`/appointments/${id}/prescription-issued`);
          showSuccess("Prescription issued.");
          break;
        case "complete":
          await axios.put(`/appointments/${id}/complete`);
          showSuccess("Appointment marked as completed.");
          break;
        case "cancel":
          await axios.put(`/appointments/${id}/cancel`,
            { reason: reason || null });
          showSuccess("Appointment cancelled.");
          break;
        case "no-show":
          await axios.put(`/appointments/${id}/no-show`,
            { reason: reason || null });
          showSuccess("Marked as no-show.");
          break;

        case "delete":
          await axios.delete(`/appointments/${id}`);
          showSuccess("Appointment deleted.");
          break;
        default:
          break;
      }
      setActionTarget(null);
      setActionType("");
      fetchAppointments();
    } catch (e) {
      alert(e.response?.data?.message || "Action failed.");
    } finally {
      setSubmitting(false);
    }
  };

  // ── Counts for tabs ────────────────────────────────────────────────────────

  const counts = STATUS_FILTERS.reduce((acc, s) => {
    acc[s] = s === "ALL"
      ? appointments.length
      : appointments.filter(a => a.status === s).length;
    return acc;
  }, {});

  const StatusBadge = ({ status }) => {
    const cfg = STATUS_CONFIG[status] || {
      bg: "bg-gray-100",
      text: "text-gray-600",
      label: status || "Unknown",
    };
    return (
      <span className={`text-xs font-medium px-2 py-0.5 rounded-full
        ${cfg.bg} ${cfg.text}`}>
        {cfg.label}
      </span>
    );
  };


  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">
        <main className="max-w-7xl mx-auto px-6 py-8">

          {/* Header */}
          <div className="flex justify-between items-start mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">Appointments</h1>
            <p className="text-gray-500 text-sm mt-1">
              Manage and approve clinic appointments
            </p>
          </div>
          <button onClick={() => setShowForm(true)}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg
              text-sm hover:bg-blue-700">
            + Schedule Appointment
          </button>
        </div>

        {/* Success message */}
        {successMsg && (
          <div role="status" aria-live="polite"
            className="bg-green-50 border border-green-200 text-green-700
              rounded-lg p-3 mb-4 text-sm">
            ✅ {successMsg}
          </div>
        )}

        {/* Error */}
        {error && (
          <div role="alert"
            className="bg-red-50 border border-red-200 text-red-600
              rounded-lg p-3 mb-4 text-sm flex justify-between items-center">
            <span>{error}</span>
            <button onClick={fetchAppointments}
              className="underline ml-3 whitespace-nowrap">Retry</button>
          </div>
        )}

        {/* Status filter tabs */}
        <div className="flex gap-2 mb-6 overflow-x-auto pb-1">
          {STATUS_FILTERS.map(s => (
            <button key={s}
              onClick={() => setStatusFilter(s)}
              className={`px-3 py-1.5 rounded-lg text-sm whitespace-nowrap
                border transition-colors
                ${statusFilter === s
                  ? "bg-blue-600 text-white border-blue-600"
                  : "bg-white text-gray-600 border-gray-300 hover:bg-gray-50"
                }`}>
              {s === "ALL" ? "All" : STATUS_CONFIG[s]?.label || s}
              <span className={`ml-1.5 text-xs px-1.5 py-0.5 rounded-full
                ${statusFilter === s
                  ? "bg-blue-500 text-white"
                  : "bg-gray-100 text-gray-600"}`}>
                {counts[s] || 0}
              </span>
            </button>
          ))}
        </div>

        {/* Loading skeleton */}
        {loading ? (
          <div className="space-y-3">
            {[1,2,3].map(i => (
              <div key={i}
                className="h-24 bg-gray-200 animate-pulse rounded-xl" />
            ))}
          </div>
        ) : appointments.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <p className="text-5xl mb-3">📅</p>
            <p className="font-medium text-gray-500">
              No {statusFilter !== "ALL"
                ? STATUS_CONFIG[statusFilter]?.label.toLowerCase()
                : ""} appointments found
            </p>
          </div>
        ) : (
          /* Appointments list */
          <div className="space-y-3">
            {appointments.map(a => (
              <div key={a.id}
                className="bg-white border border-gray-200 rounded-xl
                  p-4 shadow-sm">
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <StatusBadge status={a.status} />
                      <span className="text-xs text-gray-400">
                        #{a.id}
                      </span>
                    </div>
                  <div className="grid grid-cols-2 md:grid-cols-4
                      gap-3 text-sm">
                      <div>
                        <p className="text-gray-500 text-xs">Date</p>
                        <p className="font-medium text-gray-800">
                          {a.appointmentDate
                            ? new Date(a.appointmentDate + "T00:00:00")
                                .toLocaleDateString("en-PH", {
                                  month: "short", day: "numeric",
                                  year: "numeric"
                                })
                            : "—"}
                        </p>
                      </div>
                      <div>
                        <p className="text-gray-500 text-xs">Time</p>
                        <p className="font-medium text-gray-800">
                          {a.appointmentTime
                            ? a.appointmentTime.slice(0, 5)
                            : "—"}
                        </p>
                      </div>
                      <div>
                        <p className="text-gray-500 text-xs">Doctor</p>
                        <p className="font-medium text-gray-800">
                          {a.doctorId ? `Doctor #${a.doctorId}` : "—"}
                        </p>
                        {a.appointmentType && (
                          <p className="text-xs text-gray-400">
                            {a.appointmentType}
                          </p>
                        )}
                      </div>
                      <div>
                        <p className="text-gray-500 text-xs">Appointment Type</p>
                        <p className="font-medium text-gray-800">
                          {a.appointmentType || "—"}
                        </p>
                      </div>

                      {!isPatient && (
                        <div className="md:col-span-2">
                          <p className="text-gray-500 text-xs">Patient</p>
                          <p className="font-medium text-gray-800">
                            {a.patientName || `Patient #${a.patientId}`}
                          </p>
                          <p className="text-xs text-gray-400">
                            {a.patientNumber}
                          </p>
                        </div>
                      )}

                      {a.remarks && (
                        <div className={isPatient ? "md:col-span-2" : "md:col-span-4"}>
                          <p className="text-gray-500 text-xs">Remarks</p>
                          <p className="text-gray-700 text-sm truncate
                            max-w-xs">
                            {a.remarks}
                          </p>
                        </div>
                      )}
                    </div>

                  </div>

                  {/* Action buttons */}
                  <div className="flex flex-col gap-1.5 ml-4 min-w-fit" role="group"
                    aria-label={`Actions for appointment ${a.id}`}>
                    {isStaff && a.status === "REQUESTED" && (
                      <>
                        <button
                          onClick={() => openAction(a, "approve")}
                          aria-label="Approve appointment"
                          className="bg-blue-600 text-white px-3 py-1
                            rounded-lg text-xs hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-300">
                          ✓ Approve
                        </button>
                        <button
                          onClick={() => openAction(a, "reject")}
                          aria-label="Reject appointment"
                          className="bg-red-500 text-white px-3 py-1
                            rounded-lg text-xs hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-300">
                          ✗ Reject
                        </button>
                      </>
                    )}
                    {isStaff && a.status === "PENDING_APPROVAL" && (
                      <>
                        <button
                          onClick={() => openAction(a, "approve")}
                          aria-label="Approve appointment"
                          className="bg-blue-600 text-white px-3 py-1
                            rounded-lg text-xs hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-300">
                          ✓ Approve
                        </button>
                        <button
                          onClick={() => openAction(a, "reject")}
                          aria-label="Reject appointment"
                          className="bg-red-500 text-white px-3 py-1
                            rounded-lg text-xs hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-300">
                          ✗ Reject
                        </button>
                      </>
                    )}

                    {a.status === "APPROVED" && isStaff && (
                      <button
                        onClick={() => openAction(a, "check-in")}
                        aria-label="Check in appointment"
                        className="bg-indigo-600 text-white px-3 py-1
                          rounded-lg text-xs hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-300">
                        ✓ Check-in
                      </button>
                    )}

                    {a.status === "CHECKED_IN" && isStaff && (
                      <button
                        onClick={() => openAction(a, "waiting")}
                        aria-label="Move appointment to waiting"
                        className="bg-purple-600 text-white px-3 py-1
                          rounded-lg text-xs hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-purple-300">
                        ✓ Waiting
                      </button>
                    )}

                    {a.status === "WAITING" && isStaff && (
                      <button
                        onClick={() => openAction(a, "in-consultation")}
                        aria-label="Start consultation"
                        className="bg-cyan-600 text-white px-3 py-1
                          rounded-lg text-xs hover:bg-cyan-700 focus:outline-none focus:ring-2 focus:ring-cyan-300">
                        ✓ Start consult
                      </button>
                    )}

                    {a.status === "IN_CONSULTATION" && isStaff && (
                      <button
                        onClick={() => openAction(a, "prescription-issued")}
                        aria-label="Mark prescription issued"
                        className="bg-teal-600 text-white px-3 py-1
                          rounded-lg text-xs hover:bg-teal-700 focus:outline-none focus:ring-2 focus:ring-teal-300">
                        ✓ Prescription issued
                      </button>
                    )}

                    {(a.status === "IN_CONSULTATION" || a.status === "PRESCRIPTION_ISSUED") && isStaff && (
                      <button
                        onClick={() => openAction(a, "complete")}
                        aria-label="Complete appointment"
                        className="bg-green-600 text-white px-3 py-1
                          rounded-lg text-xs hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-300">
                        ✓ Complete
                      </button>
                    )}

                    {a.status === "APPROVED" && isStaff && (
                      <button
                        onClick={() => openAction(a, "cancel")}
                        aria-label="Cancel appointment"
                        className="border border-yellow-400 text-yellow-700
                          px-3 py-1 rounded-lg text-xs hover:bg-yellow-50 focus:outline-none focus:ring-2 focus:ring-yellow-300">
                        Cancel
                      </button>
                    )}

                    {(a.status === "APPROVED" || a.status === "WAITING") && isStaff && (
                      <button
                        onClick={() => openAction(a, "no-show")}
                        aria-label="Mark appointment as no-show"
                        className="border border-gray-400 text-gray-700
                          px-3 py-1 rounded-lg text-xs hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-300">
                        Mark no-show
                      </button>
                    )}


                    {isAdmin && (
                      <button
                        onClick={() => openAction(a, "delete")}
                        aria-label="Delete appointment"
                        className="border border-red-300 text-red-500
                          px-3 py-1 rounded-lg text-xs hover:bg-red-50 focus:outline-none focus:ring-2 focus:ring-red-300">
                        Delete
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}


        {/* Schedule Appointment modal */}
        {showForm && (
      <Modal title="Schedule Appointment"
            onClose={() => setShowForm(false)}>
            <form onSubmit={handleSubmit} className="space-y-3">
              {/* Edit C — the form field in the modal */}
              <FormField label="Patient Number *" field="patientNumber"
                form={form} setForm={setForm}
                type="text" required placeholder="e.g. PT-00002" />

              {/* Doctor dropdown (required for AppointmentDto.doctorId) */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Doctor *
                </label>
                <select
                  value={form.doctorId}
                  onChange={(e) => setForm({ ...form, doctorId: e.target.value })}
                  required
                  className="border border-gray-300 rounded-lg p-2 w-full text-sm
                    focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="">{doctorsLoading ? "Loading doctors..." : "Select a doctor"}</option>
                  {(doctors || []).map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.name || d.username || d.fullName || `Doctor #${d.id}`}
                    </option>
                  ))}
                </select>
                {doctorsError && <p className="text-red-500 text-sm mt-1">{doctorsError}</p>}
              </div>

              <FormField label="Date *" field="appointmentDate"
                form={form} setForm={setForm} type="date" required />
              <FormField label="Time *" field="appointmentTime"
                form={form} setForm={setForm} type="time" required />

              <div>
                <label className="block text-sm font-medium
                  text-gray-700 mb-1">Remarks</label>
                <textarea
                  rows={3}
                  value={form.remarks}
                  onChange={e => setForm({ ...form, remarks: e.target.value })}
                  placeholder="Optional notes or reason for visit"
                  className="border border-gray-300 rounded-lg p-2
                    w-full text-sm focus:outline-none focus:ring-2
                    focus:ring-blue-500"
                />
              </div>
              {formError && (
                <p className="text-red-500 text-sm">{formError}</p>
              )}
              <div className="flex justify-end gap-2 pt-2">
                <button type="button" onClick={() => setShowForm(false)}
                  className="px-4 py-2 rounded-lg bg-gray-200 text-sm">
                  Cancel
                </button>
                <button type="submit" disabled={submitting}
                  className="px-4 py-2 rounded-lg bg-blue-600
                    text-white text-sm disabled:opacity-60">
                  {submitting ? "Saving..." : "Schedule"}
                </button>
              </div>
            </form>
          </Modal>
        )}

        {/* Action confirmation modal */}
        {actionTarget && (
          <Modal
            title={
              actionType === "approve"            ? "Approve Appointment"   :
              actionType === "reject"             ? "Reject Appointment"    :
              actionType === "check-in"           ? "Check-In Appointment"  :
              actionType === "waiting"            ? "Move to Waiting"       :
              actionType === "in-consultation"    ? "Start Consultation"    :
              actionType === "prescription-issued"? "Prescription Issued"   :
              actionType === "complete"           ? "Complete Appointment"  :
              actionType === "cancel"             ? "Cancel Appointment"    :
              actionType === "no-show"            ? "Mark as No-Show"       :
              "Delete Appointment"
            }
            onClose={() => setActionTarget(null)}>

            <div className="mb-4">
              <p className="text-gray-600 text-sm mb-3">
                {(actionType === "approve" || actionType === "check-in" ||
                  actionType === "waiting" || actionType === "in-consultation" ||
                  actionType === "prescription-issued" || actionType === "complete" ||
                  actionType === "no-show") && (
                  <>{actionLabel(actionType)} for{" "}
                    <strong>{actionTarget.patientName || `Patient #${actionTarget.patientId}`}</strong>
                    {actionTarget.appointmentDate ? <> on <strong>{actionTarget.appointmentDate}</strong></> : null}?</>
                )}
                {actionType === "reject" && (
                  <>Reject appointment for{" "}
                    <strong>{actionTarget.patientName || `Patient #${actionTarget.patientId}`}</strong>?</>
                )}
                {actionType === "cancel" && (
                  <>Cancel appointment for{" "}
                    <strong>{actionTarget.patientName || `Patient #${actionTarget.patientId}`}</strong>?</>
                )}
                {actionType === "delete" && (
                  <>Permanently delete appointment for{" "}
                    <strong>{actionTarget.patientName || `Patient #${actionTarget.patientId}`}</strong>?
                    This cannot be undone.</>
                )}
              </p>

              {/* Reason input for reject, cancel and no-show */}
              {["reject", "cancel", "no-show"].includes(actionType) && (
                <div>
                  <label className="block text-sm font-medium
                    text-gray-700 mb-1">
                    Reason {(actionType === "reject" || actionType === "no-show")
                      ? "(optional)" : "(optional)"}
                  </label>
                  <textarea
                    rows={2}
                    value={reason}
                    onChange={e => setReason(e.target.value)}
                    placeholder="Enter reason..."
                    className="border border-gray-300 rounded-lg p-2
                      w-full text-sm"
                  />
                </div>
              )}
            </div>

            <div className="flex justify-end gap-2">
              <button onClick={() => setActionTarget(null)}
                disabled={submitting}
                className="px-4 py-2 rounded-lg bg-gray-200 text-sm
                  disabled:opacity-60 focus:outline-none focus:ring-2 focus:ring-gray-300">
                Cancel
              </button>
              <button
                onClick={handleAction}
                disabled={submitting}
                className={`px-4 py-2 rounded-lg text-white text-sm
                  disabled:opacity-60 focus:outline-none focus:ring-2 focus:ring-offset-1
                  ${actionType === "approve"  ? "bg-blue-600"  :
                    actionType === "check-in" ? "bg-indigo-600" :
                    actionType === "waiting"  ? "bg-purple-600" :
                    actionType === "in-consultation" ? "bg-cyan-600" :
                    actionType === "prescription-issued" ? "bg-teal-600" :
                    actionType === "complete" ? "bg-green-600" :
                    actionType === "reject"   ? "bg-red-500"   :
                    actionType === "no-show"  ? "bg-gray-600" :
                    actionType === "cancel"   ? "bg-yellow-600" :
                    "bg-red-600"}`}>
                {submitting ? "Processing..." :
                  actionType === "approve"  ? "Approve"  :
                  actionType === "reject"   ? "Reject"   :
                  actionType === "check-in" ? "Check-In" :
                  actionType === "waiting"  ? "Move to Waiting" :
                  actionType === "in-consultation" ? "Start Consultation" :
                  actionType === "prescription-issued" ? "Prescription Issued" :
                  actionType === "complete" ? "Complete" :
                  actionType === "no-show"  ? "Mark No-Show" :
                  actionType === "cancel"   ? "Cancel Appointment" :
                  "Delete"}
              </button>
            </div>
          </Modal>
        )}
        </main>
      </div>
    </Layout>
  );
}

// ── Shared components ─────────────────────────────────────────────────────────

function FormField({ label, field, form, setForm, type = "text",
                     required = false, placeholder = "" }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        {label}
      </label>
      <input
        type={type}
        value={form[field]}
        onChange={e => setForm({ ...form, [field]: e.target.value })}
        placeholder={placeholder}
        required={required}
        className="border border-gray-300 rounded-lg p-2 w-full text-sm
          focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
    </div>
  );
}

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-40 flex
      items-center justify-center z-50 p-4">
      <div role="dialog" aria-modal="true" aria-label={title}
        className="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4
          max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center p-4 border-b">
          <h2 className="text-lg font-semibold">{title}</h2>
          <button onClick={onClose} aria-label="Close dialog"
            className="text-gray-400 hover:text-gray-600 text-xl
              focus:outline-none focus:ring-2 focus:ring-blue-300 rounded">
            ✕
          </button>
        </div>
        <div className="p-4">{children}</div>
      </div>
    </div>
  );
}