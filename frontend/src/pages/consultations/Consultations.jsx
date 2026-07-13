import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import Layout from "../../widgets/layout/Layout";
import axios from "../../shared/api/axios";

export default function Consultations() {
  const [consultations, setConsultations] = useState([]);
  const [loading, setLoading]             = useState(false);
  const [error, setError]                 = useState("");
  const [patientId, setPatientId]         = useState("");
  const [searchedId, setSearchedId]       = useState(null);
  const [showForm, setShowForm]           = useState(false);
  const [editTarget, setEditTarget]       = useState(null);
  const [deleteTarget, setDeleteTarget]   = useState(null);
  const [form, setForm] = useState({
    patientId: "",
    chiefComplaint: "",
    vitalSigns: "",
    diagnosis: "",
    treatment: "",
    notes: "",
    consultationDate: "",
  });

  const fetchConsultations = async (pid) => {
    if (!pid) return;
    setLoading(true);
    setError("");
    try {
      const res = await axios.get(`/consultations/patient/${pid}`);
      setConsultations(res.data);
      setSearchedId(pid);
    } catch (e) {
      setError("Failed to load consultations.");
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => setForm({
    patientId: searchedId || "",
    chiefComplaint: "",
    vitalSigns: "",
    diagnosis: "",
    treatment: "",
    notes: "",
    consultationDate: "",
  });

  const openAdd = () => {
    resetForm();
    setEditTarget(null);
    setShowForm(true);
  };

  const openEdit = (c) => {
    setForm({
      patientId:        c.patientId,
      chiefComplaint:   c.chiefComplaint  || "",
      vitalSigns:       c.vitalSigns      || "",
      diagnosis:        c.diagnosis       || "",
      treatment:        c.treatment       || "",
      notes:            c.notes           || "",
      consultationDate: c.consultationDate
        ? c.consultationDate.slice(0, 16) : "",
    });
    setEditTarget(c);
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = {
      ...form,
      patientId: Number(form.patientId),
      consultationDate: form.consultationDate
        ? new Date(form.consultationDate).toISOString()
        : new Date().toISOString(),
    };
    try {
      if (editTarget) {
        await axios.put(`/consultations/${editTarget.id}`, payload);
      } else {
        await axios.post("/consultations", payload);
      }
      setShowForm(false);
      fetchConsultations(searchedId);
    } catch (e) {
      alert(e.response?.data?.message || "Something went wrong.");
    }
  };

  const handleDelete = async () => {
    try {
      await axios.delete(`/consultations/${deleteTarget.id}`);
      setDeleteTarget(null);
      fetchConsultations(searchedId);
    } catch {
      alert("Failed to delete consultation.");
    }
  };

  const field = (label, key, opts = {}) => (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      {opts.multiline ? (
        <textarea
          rows={3}
          value={form[key]}
          onChange={e => setForm({ ...form, [key]: e.target.value })}
          placeholder={opts.placeholder || ""}
          className="border border-gray-300 rounded p-2 w-full text-sm"
        />
      ) : (
        <input
          type={opts.type || "text"}
          value={form[key]}
          onChange={e => setForm({ ...form, [key]: e.target.value })}
          placeholder={opts.placeholder || ""}
          required={opts.required}
          className="border border-gray-300 rounded p-2 w-full text-sm"
        />
      )}
    </div>
  );

  return (
    <Layout>
    <div className="p-6 max-w-5xl mx-auto">
      <h1 className="text-2xl font-bold mb-6">Consultations</h1>

      {/* Search bar */}
      <div className="flex gap-2 mb-6">
        <input
          placeholder="Enter Patient ID"
          value={patientId}
          onChange={e => setPatientId(e.target.value)}
          onKeyDown={e => e.key === "Enter" && fetchConsultations(patientId)}
          className="border border-gray-300 rounded p-2 w-48 text-sm"
        />
        <button
          onClick={() => fetchConsultations(patientId)}
          className="bg-blue-600 text-white px-4 py-2 rounded text-sm"
        >
          Search
        </button>
        {searchedId && (
          <button
            onClick={openAdd}
            className="bg-green-600 text-white px-4 py-2 rounded text-sm ml-auto"
          >
            + Add Consultation
          </button>
        )}
      </div>

      {/* State feedback */}
      {loading && <p className="text-gray-500">Loading...</p>}
      {error   && <p className="text-red-500">{error}</p>}

      {/* Consultation cards */}
      {!loading && consultations.length === 0 && searchedId && (
        <p className="text-gray-400 text-center mt-10">
          No consultations found for Patient #{searchedId}
        </p>
      )}

      <div className="space-y-4">
        {consultations.map(c => (
          <div key={c.id}
            className="border border-gray-200 rounded-lg p-4 shadow-sm bg-white"
          >
            <div className="flex justify-between items-start mb-3">
              <div>
                <span className="font-semibold text-lg text-gray-800">
                  {c.chiefComplaint}
                </span>
                <p className="text-xs text-gray-400 mt-0.5">
                  {c.consultationDate
                    ? new Date(c.consultationDate).toLocaleString()
                    : "—"}
                </p>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => openEdit(c)}
                  className="text-sm text-blue-600 hover:underline"
                >
                  Edit
                </button>
                <button
                  onClick={() => setDeleteTarget(c)}
                  className="text-sm text-red-500 hover:underline"
                >
                  Delete
                </button>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 text-sm">
              <InfoRow label="Vital Signs"  value={c.vitalSigns} />
              <InfoRow label="Patient"      value={c.patientName} />
              <InfoRow label="Diagnosis"    value={c.diagnosis}  className="col-span-2" />
              <InfoRow label="Treatment"    value={c.treatment}  className="col-span-2" />
              {c.notes && (
                <div className="col-span-2 mt-1">
                  <span className="text-gray-500 text-xs">Notes: </span>
                  <span className="italic text-gray-600 text-xs">{c.notes}</span>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Add / Edit modal */}
      {showForm && (
        <Modal title={editTarget ? "Edit Consultation" : "Add Consultation"}
          onClose={() => setShowForm(false)}>
          <form onSubmit={handleSubmit} className="space-y-3">
            {field("Patient ID",       "patientId",      { required: true })}
            {field("Chief Complaint",  "chiefComplaint", { required: true })}
            {field("Vital Signs",      "vitalSigns",     { placeholder: "BP: 120/80, Temp: 36.5°C" })}
            {field("Diagnosis",        "diagnosis",      { multiline: true })}
            {field("Treatment",        "treatment",      { multiline: true })}
            {field("Notes",            "notes",          { multiline: true })}
            {field("Consultation Date","consultationDate",{ type: "datetime-local" })}
            <ModalActions
              onCancel={() => setShowForm(false)}
              submitLabel={editTarget ? "Update" : "Save"}
            />
          </form>
        </Modal>
      )}

      {/* Delete confirmation */}
      {deleteTarget && (
        <Modal title="Confirm Delete" onClose={() => setDeleteTarget(null)}>
          <p className="text-gray-600 mb-4">
            Delete consultation for{" "}
            <strong>{deleteTarget.chiefComplaint}</strong>? This cannot be undone.
          </p>
          <div className="flex justify-end gap-2">
            <button
              onClick={() => setDeleteTarget(null)}
              className="px-4 py-2 rounded bg-gray-200 text-sm"
            >
              Cancel
            </button>
            <button
              onClick={handleDelete}
              className="px-4 py-2 rounded bg-red-600 text-white text-sm"
            >
              Delete
            </button>
          </div>
        </Modal>
      )}
    </div>
    </Layout>
  );
}

function InfoRow({ label, value, className = "" }) {
  return (
    <div className={className}>
      <span className="text-gray-500 text-xs">{label}: </span>
      <span className="text-gray-800 text-sm">{value || "—"}</span>
    </div>
  );
}

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center p-4 border-b">
          <h2 className="text-lg font-semibold">{title}</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl">✕</button>
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
        className="px-4 py-2 rounded bg-gray-200 text-sm">
        Cancel
      </button>
      <button type="submit"
        className="px-4 py-2 rounded bg-blue-600 text-white text-sm">
        {submitLabel}
      </button>
    </div>
  );
}