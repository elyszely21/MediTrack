import { useEffect, useState, useCallback } from "react";
import Layout from "../../widgets/layout/Layout";
import axios from "../../shared/api/axios";

export default function Patients() {
  const [patients, setPatients]           = useState([]);
  const [loading, setLoading]             = useState(true);
  const [error, setError]                 = useState("");
  const [searchQuery, setSearchQuery]     = useState("");
  const [showArchived, setShowArchived]   = useState(false);
  const [showForm, setShowForm]           = useState(false);
  const [editTarget, setEditTarget]       = useState(null);
  const [deleteTarget, setDeleteTarget]   = useState(null);
  const [archiveTarget, setArchiveTarget] = useState(null);
  const [submitting, setSubmitting]       = useState(false);
  const [successMsg, setSuccessMsg]       = useState("");
  const [formError, setFormError]         = useState("");
  const [form, setForm] = useState({
    patientNumber: "", firstName: "", lastName: "",
    gender: "", birthDate: "", contactNumber: "",
    emergencyContact: "", address: ""
  });

  const fetchPatients = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      let res;
      if (searchQuery.trim()) {
        res = await axios.get(
          `/patients/search?q=${encodeURIComponent(searchQuery)}&archived=${showArchived}`
        );
      } else {
        res = await axios.get(showArchived ? "/patients/archived" : "/patients");
      }
      setPatients(res.data);
    } catch {
      setError("Failed to load patients.");
    } finally {
      setLoading(false);
    }
  }, [searchQuery, showArchived]);

  useEffect(() => {
    const delay = setTimeout(() => fetchPatients(), 300);
    return () => clearTimeout(delay);
  }, [fetchPatients]);

  const resetForm = () => {
    setForm({ patientNumber:"", firstName:"", lastName:"",
      gender:"", birthDate:"", contactNumber:"",
      emergencyContact:"", address:"" });
    setFormError("");
  };

  const openAdd = () => { resetForm(); setEditTarget(null); setShowForm(true); };

  const openEdit = (p) => {
    setForm({
      patientNumber:    p.patientNumber    || "",
      firstName:        p.firstName        || "",
      lastName:         p.lastName         || "",
      gender:           p.gender           || "",
      birthDate:        p.birthDate        || "",
      contactNumber:    p.contactNumber    || "",
      emergencyContact: p.emergencyContact || "",
      address:          p.address          || "",
    });
    setEditTarget(p);
    setShowForm(true);
  };

  const showSuccess = (msg) => {
    setSuccessMsg(msg);
    setTimeout(() => setSuccessMsg(""), 3000);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError("");
    if (!form.patientNumber.trim()) { setFormError("Patient number is required."); return; }
    if (!form.firstName.trim() || !form.lastName.trim()) {
      setFormError("First and last name are required."); return;
    }
    setSubmitting(true);
    try {
      if (editTarget) {
        await axios.put(`/patients/${editTarget.id}`, form);
        showSuccess("Patient updated successfully.");
      } else {
        await axios.post("/patients", form);
        showSuccess("Patient registered successfully.");
      }
      setShowForm(false);
      resetForm();
      fetchPatients();
    } catch (e) {
      setFormError(e.response?.data?.message || "Something went wrong.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    try {
      await axios.delete(`/patients/${deleteTarget.id}`);
      setDeleteTarget(null);
      showSuccess("Patient permanently deleted.");
      fetchPatients();
    } catch { alert("Failed to delete patient."); }
  };

  const handleArchiveToggle = async () => {
    const endpoint = archiveTarget.archived
      ? `/patients/${archiveTarget.id}/unarchive`
      : `/patients/${archiveTarget.id}/archive`;
    try {
      await axios.put(endpoint);
      setArchiveTarget(null);
      showSuccess(archiveTarget.archived
        ? "Patient restored." : "Patient archived.");
      fetchPatients();
    } catch { alert("Failed to update patient status."); }
  };

  return (
    <Layout>
      {/* Header */}
      <div className="flex justify-between items-start mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Patients</h1>
          <p className="text-gray-500 text-sm mt-1">
            {showArchived ? "Showing archived patients" : "Showing active patients"}
          </p>
        </div>
        <button onClick={openAdd}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg
            text-sm hover:bg-blue-700">
          + Add Patient
        </button>
      </div>

      {successMsg && (
        <div className="bg-green-50 border border-green-200 text-green-700
          rounded-lg p-3 mb-4 text-sm">✅ {successMsg}</div>
      )}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600
          rounded-lg p-3 mb-4 text-sm flex justify-between">
          <span>{error}</span>
          <button onClick={fetchPatients} className="underline">Retry</button>
        </div>
      )}

      {/* Search + Filter */}
      <div className="flex gap-3 mb-6">
        <div className="relative flex-1">
          <span className="absolute left-3 top-1/2 -translate-y-1/2
            text-gray-400 text-sm">🔍</span>
          <input placeholder="Search by name or patient number..."
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            className="border border-gray-300 rounded-lg pl-9 pr-4 py-2
              w-full text-sm focus:outline-none focus:ring-2
              focus:ring-blue-500" />
          {searchQuery && (
            <button onClick={() => setSearchQuery("")}
              className="absolute right-3 top-1/2 -translate-y-1/2
                text-gray-400 hover:text-gray-600">✕</button>
          )}
        </div>
        <button
          onClick={() => { setShowArchived(!showArchived); setSearchQuery(""); }}
          className={`px-4 py-2 rounded-lg text-sm border transition-colors
            ${showArchived
              ? "bg-yellow-50 border-yellow-300 text-yellow-700"
              : "bg-white border-gray-300 text-gray-600 hover:bg-gray-50"}`}>
          {showArchived ? "📦 Archived" : "👥 Active"}
        </button>
      </div>

      {/* Stats */}
      <div className="bg-white border border-gray-200 rounded-xl p-4
        mb-6 shadow-sm">
        <span className="text-2xl font-bold text-blue-600">
          {patients.length}
        </span>
        <span className="text-gray-500 text-sm ml-2">
          {searchQuery ? "results" : showArchived ? "archived" : "active patients"}
        </span>
      </div>

      {/* Loading */}
      {loading ? (
        <div className="space-y-3">
          {[1,2,3,4].map(i => (
            <div key={i} className="h-16 bg-gray-200 animate-pulse rounded-xl" />
          ))}
        </div>
      ) : patients.length === 0 ? (
        <div className="text-center py-16 text-gray-400">
          <p className="text-5xl mb-3">
            {searchQuery ? "🔍" : showArchived ? "📦" : "👥"}
          </p>
          <p className="font-medium text-gray-500">
            {searchQuery ? `No patients found for "${searchQuery}"`
              : showArchived ? "No archived patients"
              : "No patients registered yet"}
          </p>
          {!searchQuery && !showArchived && (
            <button onClick={openAdd}
              className="mt-4 bg-blue-600 text-white px-4 py-2
                rounded-lg text-sm">
              Add First Patient
            </button>
          )}
        </div>
      ) : (
        <div className="bg-white border border-gray-200 rounded-xl
          shadow-sm overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                {["Patient No","Name","Gender","Birth Date",
                  "Contact","Status","Actions"].map(h => (
                  <th key={h} className="text-left p-4 text-gray-600
                    font-medium">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {patients.map(p => (
                <tr key={p.id}
                  className={`hover:bg-gray-50 ${p.archived ? "opacity-60" : ""}`}>
                  <td className="p-4">
                    <span className="bg-blue-100 text-blue-700 text-xs
                      font-medium px-2 py-0.5 rounded">
                      {p.patientNumber}
                    </span>
                  </td>
                  <td className="p-4">
                    <div className="flex items-center gap-2">
                      <div className="w-8 h-8 bg-blue-100 rounded-full
                        flex items-center justify-center text-blue-600
                        font-semibold text-sm">
                        {p.firstName?.charAt(0).toUpperCase()}
                      </div>
                      <span className="font-medium text-gray-800">
                        {p.firstName} {p.lastName}
                      </span>
                    </div>
                  </td>
                  <td className="p-4 text-gray-600">{p.gender || "—"}</td>
                  <td className="p-4 text-gray-600">{p.birthDate || "—"}</td>
                  <td className="p-4 text-gray-600">{p.contactNumber || "—"}</td>
                  <td className="p-4">
                    <span className={`text-xs font-medium px-2 py-0.5
                      rounded-full ${p.archived
                        ? "bg-yellow-100 text-yellow-700"
                        : "bg-green-100 text-green-700"}`}>
                      {p.archived ? "Archived" : "Active"}
                    </span>
                  </td>
                  <td className="p-4">
                    <div className="flex gap-2">
                      {!p.archived && (
                        <button onClick={() => openEdit(p)}
                          className="text-blue-600 hover:underline text-xs">
                          Edit
                        </button>
                      )}
                      <button onClick={() => setArchiveTarget(p)}
                        className={`text-xs hover:underline
                          ${p.archived ? "text-green-600" : "text-yellow-600"}`}>
                        {p.archived ? "Restore" : "Archive"}
                      </button>
                      <button onClick={() => setDeleteTarget(p)}
                        className="text-red-500 hover:underline text-xs">
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Add/Edit Modal */}
      {showForm && (
        <Modal title={editTarget ? "Edit Patient" : "Register Patient"}
          onClose={() => setShowForm(false)}>
          <form onSubmit={handleSubmit} className="space-y-3">
            <FormField label="Patient Number *" field="patientNumber"
              form={form} setForm={setForm}
              required disabled={!!editTarget} placeholder="e.g. P-0001" />
            <div className="grid grid-cols-2 gap-3">
              <FormField label="First Name *" field="firstName"
                form={form} setForm={setForm} required />
              <FormField label="Last Name *" field="lastName"
                form={form} setForm={setForm} required />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium
                  text-gray-700 mb-1">Gender</label>
                <select value={form.gender}
                  onChange={e => setForm({ ...form, gender: e.target.value })}
                  className="border border-gray-300 rounded-lg p-2
                    w-full text-sm">
                  <option value="">Select gender</option>
                  {["Male","Female","Other"].map(g => (
                    <option key={g}>{g}</option>
                  ))}
                </select>
              </div>
              <FormField label="Birth Date" field="birthDate"
                form={form} setForm={setForm} type="date" />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <FormField label="Contact Number" field="contactNumber"
                form={form} setForm={setForm} />
              <FormField label="Emergency Contact" field="emergencyContact"
                form={form} setForm={setForm} />
            </div>
            <FormField label="Address" field="address"
              form={form} setForm={setForm} />
            {formError && (
              <p className="text-red-500 text-sm">{formError}</p>
            )}
            <div className="flex justify-end gap-2 pt-2">
              <button type="button" onClick={() => setShowForm(false)}
                className="px-4 py-2 rounded-lg bg-gray-200 text-sm">
                Cancel
              </button>
              <button type="submit" disabled={submitting}
                className="px-4 py-2 rounded-lg bg-blue-600 text-white
                  text-sm disabled:opacity-60">
                {submitting ? "Saving..." : editTarget
                  ? "Update Patient" : "Register Patient"}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Archive Modal */}
      {archiveTarget && (
        <Modal title={archiveTarget.archived ? "Restore Patient" : "Archive Patient"}
          onClose={() => setArchiveTarget(null)}>
          <p className="text-gray-600 mb-4">
            {archiveTarget.archived
              ? <>Restore <strong>{archiveTarget.firstName} {archiveTarget.lastName}</strong> to active patients?</>
              : <>Archive <strong>{archiveTarget.firstName} {archiveTarget.lastName}</strong>? Their records will be preserved.</>
            }
          </p>
          <div className="flex justify-end gap-2">
            <button onClick={() => setArchiveTarget(null)}
              className="px-4 py-2 rounded-lg bg-gray-200 text-sm">Cancel</button>
            <button onClick={handleArchiveToggle}
              className={`px-4 py-2 rounded-lg text-white text-sm
                ${archiveTarget.archived ? "bg-green-600" : "bg-yellow-600"}`}>
              {archiveTarget.archived ? "Restore" : "Archive"}
            </button>
          </div>
        </Modal>
      )}

      {/* Delete Modal */}
      {deleteTarget && (
        <Modal title="Permanently Delete Patient"
          onClose={() => setDeleteTarget(null)}>
          <div className="bg-red-50 border border-red-200 rounded-lg
            p-3 mb-4">
            <p className="text-red-700 text-sm font-medium">
              ⚠️ This action cannot be undone
            </p>
            <p className="text-red-600 text-sm mt-1">
              All records, appointments, and bills will also be deleted.
            </p>
          </div>
          <p className="text-gray-600 mb-4 text-sm">
            Permanently delete{" "}
            <strong>{deleteTarget.firstName} {deleteTarget.lastName}</strong>{" "}
            ({deleteTarget.patientNumber})?
          </p>
          <div className="flex justify-end gap-2">
            <button onClick={() => setDeleteTarget(null)}
              className="px-4 py-2 rounded-lg bg-gray-200 text-sm">Cancel</button>
            <button onClick={handleDelete}
              className="px-4 py-2 rounded-lg bg-red-600 text-white text-sm">
              Permanently Delete
            </button>
          </div>
        </Modal>
      )}
    </Layout>
  );
}

function FormField({ label, field, form, setForm, type="text",
                     required=false, placeholder="", disabled=false }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        {label}
      </label>
      <input type={type} value={form[field]} placeholder={placeholder}
        required={required} disabled={disabled}
        onChange={e => setForm({ ...form, [field]: e.target.value })}
        className={`border border-gray-300 rounded-lg p-2 w-full text-sm
          focus:outline-none focus:ring-2 focus:ring-blue-500
          ${disabled ? "bg-gray-100 cursor-not-allowed" : ""}`} />
    </div>
  );
}

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-40 flex
      items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-lg mx-4
        max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center p-4 border-b">
          <h2 className="text-lg font-semibold">{title}</h2>
          <button onClick={onClose}
            className="text-gray-400 hover:text-gray-600 text-xl">✕</button>
        </div>
        <div className="p-4">{children}</div>
      </div>
    </div>
  );
}