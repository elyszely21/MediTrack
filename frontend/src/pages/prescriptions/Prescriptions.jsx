import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import Layout from "../components/Layout";
import axios from "../api/axios";

const STATUS_COLORS = {
  ACTIVE:    "bg-green-100 text-green-800",
  COMPLETED: "bg-blue-100 text-blue-800",
  CANCELLED: "bg-red-100 text-red-800",
};

export default function Prescriptions() {
  const [prescriptions, setPrescriptions] = useState([]);
  const [loading, setLoading]             = useState(false);
  const [error, setError]                 = useState("");
  const [patientId, setPatientId]         = useState("");
  const [searchedId, setSearchedId]       = useState(null);
  const [showForm, setShowForm]           = useState(false);
  const [editTarget, setEditTarget]       = useState(null);
  const [deleteTarget, setDeleteTarget]   = useState(null);
  const [printTarget, setPrintTarget]     = useState(null);
  const [form, setForm] = useState({
    patientId:       "",
    medicalRecordId: "",
    medication:      "",
    dosage:          "",
    frequency:       "",
    duration:        "",
    instructions:    "",
    prescribedBy:    "",
    prescribedDate:  "",
    status:          "ACTIVE",
  });

  const fetchPrescriptions = async (pid) => {
    if (!pid) return;
    setLoading(true);
    setError("");
    try {
      const res = await axios.get(`/prescriptions/patient/${pid}`);
      setPrescriptions(res.data);
      setSearchedId(pid);
    } catch {
      setError("Failed to load prescriptions.");
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => setForm({
    patientId:       searchedId || "",
    medicalRecordId: "",
    medication:      "",
    dosage:          "",
    frequency:       "",
    duration:        "",
    instructions:    "",
    prescribedBy:    "",
    prescribedDate:  new Date().toISOString().split("T")[0],
    status:          "ACTIVE",
  });

  const openAdd  = () => { resetForm(); setEditTarget(null); setShowForm(true); };

  const openEdit = (p) => {
    setForm({
      patientId:       p.patientId,
      medicalRecordId: p.medicalRecordId || "",
      medication:      p.medication      || "",
      dosage:          p.dosage          || "",
      frequency:       p.frequency       || "",
      duration:        p.duration        || "",
      instructions:    p.instructions    || "",
      prescribedBy:    p.prescribedBy    || "",
      prescribedDate:  p.prescribedDate  || "",
      status:          p.status          || "ACTIVE",
    });
    setEditTarget(p);
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = {
      ...form,
      patientId:       Number(form.patientId),
      medicalRecordId: form.medicalRecordId
        ? Number(form.medicalRecordId) : null,
    };
    try {
      if (editTarget) {
        await axios.put(`/prescriptions/${editTarget.id}`, payload);
      } else {
        await axios.post("/prescriptions", payload);
      }
      setShowForm(false);
      fetchPrescriptions(searchedId);
    } catch (e) {
      alert(e.response?.data?.message || "Something went wrong.");
    }
  };

  const handleDelete = async () => {
    try {
      await axios.delete(`/prescriptions/${deleteTarget.id}`);
      setDeleteTarget(null);
      fetchPrescriptions(searchedId);
    } catch {
      alert("Failed to delete prescription.");
    }
  };

  const handlePrint = (prescription) => {
    setPrintTarget(prescription);
    setTimeout(() => window.print(), 300);
  };

  const handlePrintAll = () => {
    setPrintTarget("all");
    setTimeout(() => window.print(), 300);
  };

  return (
    <Layout>
      {/* ── Print styles ─────────────────────────────────────────────────── */}
      <style>{`
        @media print {
          body * { visibility: hidden; }
          #print-area, #print-area * { visibility: visible; }
          #print-area {
            position: fixed;
            top: 0; left: 0;
            width: 100%;
            padding: 40px;
            font-family: Arial, sans-serif;
            font-size: 13px;
            color: #000;
          }
          .no-print { display: none !important; }
        }
      `}</style>

      {/* ── Print area (hidden on screen, visible on print) ──────────────── */}
      {printTarget && (
        <div id="print-area" className="hidden print:block">
          <div style={{ borderBottom: "2px solid #000", paddingBottom: 12,
            marginBottom: 20 }}>
            <h1 style={{ fontSize: 22, fontWeight: "bold", margin: 0 }}>
              MediTrack
            </h1>
            <p style={{ margin: "4px 0 0", color: "#555" }}>
              Clinic Records Management System
            </p>
          </div>

          <h2 style={{ fontSize: 16, marginBottom: 4 }}>
            PRESCRIPTION FORM
          </h2>
          <p style={{ color: "#555", marginBottom: 20 }}>
            Patient ID: {searchedId} &nbsp;|&nbsp;
            Printed: {new Date().toLocaleString()}
          </p>

          {(printTarget === "all" ? prescriptions : [printTarget]).map(p => (
            <div key={p.id} style={{ border: "1px solid #ddd",
              borderRadius: 6, padding: 16, marginBottom: 16 }}>
              <div style={{ display: "flex", justifyContent: "space-between",
                marginBottom: 12 }}>
                <div>
                  <p style={{ fontWeight: "bold", fontSize: 15, margin: 0 }}>
                    {p.medication}
                  </p>
                  <p style={{ color: "#555", margin: "2px 0 0" }}>
                    Status: {p.status}
                  </p>
                </div>
                <div style={{ textAlign: "right" }}>
                  <p style={{ margin: 0 }}>
                    Date: <strong>{p.prescribedDate}</strong>
                  </p>
                  <p style={{ margin: "2px 0 0" }}>
                    Rx #{p.id}
                  </p>
                </div>
              </div>

              <table style={{ width: "100%", borderCollapse: "collapse",
                marginBottom: 10 }}>
                <tbody>
                  {[
                    ["Dosage",    p.dosage    || "—"],
                    ["Frequency", p.frequency || "—"],
                    ["Duration",  p.duration  || "—"],
                  ].map(([label, val]) => (
                    <tr key={label}>
                      <td style={{ padding: "4px 8px 4px 0", color: "#555",
                        width: 120 }}>{label}:</td>
                      <td style={{ padding: "4px 0", fontWeight: "500" }}>
                        {val}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {p.instructions && (
                <div style={{ background: "#f9f9f9", borderRadius: 4,
                  padding: "8px 12px", marginBottom: 10 }}>
                  <p style={{ margin: 0, color: "#555" }}>Instructions:</p>
                  <p style={{ margin: "4px 0 0" }}>{p.instructions}</p>
                </div>
              )}

              <div style={{ borderTop: "1px solid #eee", paddingTop: 10,
                display: "flex", justifyContent: "space-between" }}>
                <p style={{ margin: 0, color: "#555" }}>
                  Prescribed by:
                </p>
                <p style={{ margin: 0, fontWeight: "bold" }}>
                  {p.prescribedBy}
                </p>
              </div>
            </div>
          ))}

          <div style={{ marginTop: 40, borderTop: "1px solid #000",
            paddingTop: 8, display: "flex",
            justifyContent: "space-between" }}>
            <p style={{ margin: 0, color: "#555" }}>
              This is a computer-generated prescription.
            </p>
            <p style={{ margin: 0 }}>
              MediTrack Clinic Records System
            </p>
          </div>
        </div>
      )}

      {/* ── Main content ──────────────────────────────────────────────────── */}
      <div className="p-6 max-w-5xl mx-auto no-print">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold">Prescriptions</h1>
          {prescriptions.length > 0 && (
            <button onClick={handlePrintAll}
              className="border border-gray-300 text-gray-600 px-4 py-2
                rounded text-sm hover:bg-gray-50 flex items-center gap-2">
              🖨️ Print All
            </button>
          )}
        </div>

        {/* Search bar */}
        <div className="flex gap-2 mb-6">
          <input
            placeholder="Enter Patient ID"
            value={patientId}
            onChange={e => setPatientId(e.target.value)}
            onKeyDown={e => e.key === "Enter" && fetchPrescriptions(patientId)}
            className="border border-gray-300 rounded p-2 w-48 text-sm"
          />
          <button onClick={() => fetchPrescriptions(patientId)}
            className="bg-blue-600 text-white px-4 py-2 rounded text-sm">
            Search
          </button>
          {searchedId && (
            <button onClick={openAdd}
              className="bg-green-600 text-white px-4 py-2 rounded
                text-sm ml-auto">
              + Add Prescription
            </button>
          )}
        </div>

        {loading && <p className="text-gray-500">Loading...</p>}
        {error   && <p className="text-red-500">{error}</p>}

        {!loading && prescriptions.length === 0 && searchedId && (
          <p className="text-gray-400 text-center mt-10">
            No prescriptions found for Patient #{searchedId}
          </p>
        )}

        {/* Prescription table */}
        {prescriptions.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse border
              border-gray-200 text-sm">
              <thead className="bg-gray-50">
                <tr>
                  {["Medication","Dosage","Frequency","Duration",
                    "Prescribed By","Date","Status","Actions"].map(h => (
                    <th key={h} className="border border-gray-200 p-2
                      text-left font-medium text-gray-600">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {prescriptions.map(p => (
                  <tr key={p.id} className="hover:bg-gray-50">
                    <td className="border border-gray-200 p-2 font-medium">
                      {p.medication}
                    </td>
                    <td className="border border-gray-200 p-2">
                      {p.dosage    || "—"}
                    </td>
                    <td className="border border-gray-200 p-2">
                      {p.frequency || "—"}
                    </td>
                    <td className="border border-gray-200 p-2">
                      {p.duration  || "—"}
                    </td>
                    <td className="border border-gray-200 p-2">
                      {p.prescribedBy}
                    </td>
                    <td className="border border-gray-200 p-2">
                      {p.prescribedDate}
                    </td>
                    <td className="border border-gray-200 p-2">
                      <span className={`px-2 py-0.5 rounded text-xs
                        font-medium ${STATUS_COLORS[p.status]
                        || "bg-gray-100 text-gray-700"}`}>
                        {p.status}
                      </span>
                    </td>
                    <td className="border border-gray-200 p-2">
                      <div className="flex gap-2">
                        <button onClick={() => openEdit(p)}
                          className="text-blue-600 hover:underline text-xs">
                          Edit
                        </button>
                        <button onClick={() => handlePrint(p)}
                          className="text-green-600 hover:underline text-xs">
                          Print
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

        {/* Add / Edit modal */}
        {showForm && (
          <Modal title={editTarget ? "Edit Prescription" : "Add Prescription"}
            onClose={() => setShowForm(false)}>
            <form onSubmit={handleSubmit} className="space-y-3">
              <FormField label="Patient ID *" field="patientId"
                form={form} setForm={setForm} required />
              <FormField label="Medical Record ID" field="medicalRecordId"
                form={form} setForm={setForm} />
              <FormField label="Medication *" field="medication"
                form={form} setForm={setForm} required />
              <div className="grid grid-cols-2 gap-3">
                <FormField label="Dosage" field="dosage"
                  form={form} setForm={setForm} />
                <FormField label="Frequency" field="frequency"
                  form={form} setForm={setForm}
                  placeholder="e.g. Twice daily" />
                <FormField label="Duration" field="duration"
                  form={form} setForm={setForm}
                  placeholder="e.g. 7 days" />
                <FormField label="Prescribed Date *" field="prescribedDate"
                  form={form} setForm={setForm} type="date" required />
              </div>
              <FormField label="Prescribed By *" field="prescribedBy"
                form={form} setForm={setForm} required />
              <FormField label="Instructions" field="instructions"
                form={form} setForm={setForm} multiline />
              <div>
                <label className="block text-sm font-medium
                  text-gray-700 mb-1">Status</label>
                <select value={form.status}
                  onChange={e => setForm({ ...form, status: e.target.value })}
                  className="border border-gray-300 rounded p-2
                    w-full text-sm">
                  {["ACTIVE","COMPLETED","CANCELLED"].map(s => (
                    <option key={s}>{s}</option>
                  ))}
                </select>
              </div>
              <ModalActions onCancel={() => setShowForm(false)}
                submitLabel={editTarget ? "Update" : "Save"} />
            </form>
          </Modal>
        )}

        {/* Delete confirmation */}
        {deleteTarget && (
          <Modal title="Confirm Delete"
            onClose={() => setDeleteTarget(null)}>
            <p className="text-gray-600 mb-4">
              Delete prescription for{" "}
              <strong>{deleteTarget.medication}</strong>?
            </p>
            <div className="flex justify-end gap-2">
              <button onClick={() => setDeleteTarget(null)}
                className="px-4 py-2 rounded bg-gray-200 text-sm">
                Cancel
              </button>
              <button onClick={handleDelete}
                className="px-4 py-2 rounded bg-red-600 text-white text-sm">
                Delete
              </button>
            </div>
          </Modal>
        )}
      </div>
    </Layout>
  );
}

// ── Shared components ─────────────────────────────────────────────────────────

function FormField({ label, field, form, setForm, type = "text",
                     required = false, placeholder = "", multiline = false }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        {label}
      </label>
      {multiline ? (
        <textarea rows={3} value={form[field]} placeholder={placeholder}
          required={required}
          onChange={e => setForm({ ...form, [field]: e.target.value })}
          className="border border-gray-300 rounded p-2 w-full text-sm" />
      ) : (
        <input type={type} value={form[field]} placeholder={placeholder}
          required={required}
          onChange={e => setForm({ ...form, [field]: e.target.value })}
          className="border border-gray-300 rounded p-2 w-full text-sm" />
      )}
    </div>
  );
}

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-40 flex
      items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-lg mx-4
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

function ModalActions({ onCancel, submitLabel }) {
  return (
    <div className="flex justify-end gap-2 pt-2">
      <button type="button" onClick={onCancel}
        className="px-4 py-2 rounded bg-gray-200 text-sm">Cancel</button>
      <button type="submit"
        className="px-4 py-2 rounded bg-blue-600 text-white text-sm">
        {submitLabel}
      </button>
    </div>
  );
}