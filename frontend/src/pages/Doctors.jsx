import { useEffect, useState } from "react";
import axios from "../api/axios";
import Navbar from '../components/Navbar';

export default function Doctors() {
  const [doctors, setDoctors]         = useState([]);
  const [loading, setLoading]         = useState(true);
  const [showForm, setShowForm]       = useState(false);
  const [editTarget, setEditTarget]   = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [error, setError]             = useState("");
  const [form, setForm] = useState({
    fullName: "", email: "", phoneNumber: "",
    password: "", specialization: "", licenseNumber: ""
  });

  const fetchDoctors = async () => {
    setLoading(true);
    try {
      const res = await axios.get("/doctors");
      setDoctors(res.data);
    } catch {
      setError("Failed to load doctors.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchDoctors(); }, []);

  const resetForm = () => setForm({
    fullName: "", email: "", phoneNumber: "",
    password: "", specialization: "", licenseNumber: ""
  });

  const openAdd = () => {
    resetForm();
    setEditTarget(null);
    setShowForm(true);
  };

  const openEdit = (d) => {
    setForm({
      fullName:       d.fullName       || "",
      email:          d.email          || "",
      phoneNumber:    d.phoneNumber    || "",
      password:       "",
      specialization: d.specialization || "",
      licenseNumber:  d.licenseNumber  || ""
    });
    setEditTarget(d);
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editTarget) {
        await axios.put(`/doctors/${editTarget.id}`, form);
      } else {
        await axios.post("/doctors", form);
      }
      setShowForm(false);
      resetForm();
      fetchDoctors();
    } catch (e) {
      alert(e.response?.data?.message || "Something went wrong.");
    }
  };

  const handleDelete = async () => {
    try {
      await axios.delete(`/doctors/${deleteTarget.id}`);
      setDeleteTarget(null);
      fetchDoctors();
    } catch {
      alert("Failed to delete doctor.");
    }
  };

  const SPECIALIZATIONS = [
    "General Medicine", "Pediatrics", "Cardiology",
    "Dermatology", "Orthopedics", "Neurology",
    "Ophthalmology", "Obstetrics & Gynecology", "Other"
  ];

  return (
    <div className="p-6 max-w-6xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Doctors</h1>
          <p className="text-gray-500 text-sm mt-1">
            Manage clinic doctors and their accounts
          </p>
        </div>
        <button onClick={openAdd}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm
            hover:bg-blue-700">
          + Register Doctor
        </button>
      </div>

      {error && <p className="text-red-500 mb-4">{error}</p>}

      {loading ? (
        <div className="space-y-3">
          {[1,2,3].map(i => (
            <div key={i} className="h-20 bg-gray-200 animate-pulse rounded-lg" />
          ))}
        </div>
      ) : doctors.length === 0 ? (
        <div className="text-center py-16 text-gray-400">
          <p className="text-4xl mb-3">👨‍⚕️</p>
          <p className="font-medium">No doctors registered yet</p>
          <p className="text-sm mt-1">Click Register Doctor to add one</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {doctors.map(d => (
            <div key={d.id}
              className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
              <div className="flex justify-between items-start mb-3">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-blue-100 rounded-full flex
                    items-center justify-center text-blue-600 font-bold">
                    {d.fullName?.charAt(0).toUpperCase()}
                  </div>
                  <div>
                    <p className="font-semibold text-gray-800">Dr. {d.fullName}</p>
                    <p className="text-xs text-gray-500">{d.email}</p>
                  </div>
                </div>
                <span className="bg-blue-100 text-blue-700 text-xs
                  px-2 py-0.5 rounded-full font-medium">
                  Doctor
                </span>
              </div>

              <div className="space-y-1 text-sm mb-4">
                <p>
                  <span className="text-gray-500">Specialization: </span>
                  <span className="text-gray-800">
                    {d.specialization || "General Medicine"}
                  </span>
                </p>
                <p>
                  <span className="text-gray-500">License No: </span>
                  <span className="text-gray-800">
                    {d.licenseNumber || "—"}
                  </span>
                </p>
                <p>
                  <span className="text-gray-500">Phone: </span>
                  <span className="text-gray-800">{d.phoneNumber}</span>
                </p>
                <p>
                  <span className="text-gray-500">Joined: </span>
                  <span className="text-gray-800">
                    {d.createdAt
                      ? new Date(d.createdAt).toLocaleDateString()
                      : "—"}
                  </span>
                </p>
              </div>

              <div className="flex gap-2">
                <button onClick={() => openEdit(d)}
                  className="flex-1 border border-blue-300 text-blue-600
                    py-1.5 rounded-lg text-sm hover:bg-blue-50">
                  Edit
                </button>
                <button onClick={() => setDeleteTarget(d)}
                  className="flex-1 border border-red-300 text-red-500
                    py-1.5 rounded-lg text-sm hover:bg-red-50">
                  Remove
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add / Edit modal */}
      {showForm && (
        <Modal title={editTarget ? "Edit Doctor" : "Register Doctor"}
          onClose={() => setShowForm(false)}>
          <form onSubmit={handleSubmit} className="space-y-3">
            <FormField label="Full Name *"     field="fullName"
              form={form} setForm={setForm} required />
            <FormField label="Email *"         field="email"
              form={form} setForm={setForm} type="email"
              required disabled={!!editTarget} />
            <FormField label="Phone Number *"  field="phoneNumber"
              form={form} setForm={setForm} required />
            <FormField
              label={editTarget ? "New Password (leave blank to keep)" : "Password *"}
              field="password"
              form={form} setForm={setForm}
              type="password" required={!editTarget} />
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Specialization
              </label>
              <select value={form.specialization}
                onChange={e => setForm({ ...form, specialization: e.target.value })}
                className="border border-gray-300 rounded-lg p-2 w-full text-sm">
                <option value="">Select specialization</option>
                {SPECIALIZATIONS.map(s => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </select>
            </div>
            <FormField label="License Number"  field="licenseNumber"
              form={form} setForm={setForm}
              placeholder="e.g. PRC-12345" />
            <ModalActions
              onCancel={() => setShowForm(false)}
              submitLabel={editTarget ? "Update Doctor" : "Register Doctor"} />
          </form>
        </Modal>
      )}

      {/* Delete confirmation */}
      {deleteTarget && (
        <Modal title="Confirm Remove" onClose={() => setDeleteTarget(null)}>
          <p className="text-gray-600 mb-4">
            Remove <strong>Dr. {deleteTarget.fullName}</strong> from the system?
            This cannot be undone.
          </p>
          <div className="flex justify-end gap-2">
            <button onClick={() => setDeleteTarget(null)}
              className="px-4 py-2 rounded-lg bg-gray-200 text-sm">
              Cancel
            </button>
            <button onClick={handleDelete}
              className="px-4 py-2 rounded-lg bg-red-600 text-white text-sm">
              Remove Doctor
            </button>
          </div>
        </Modal>
      )}
    </div>
  );
}

// ── Shared components ─────────────────────────────────────────────────────────

function FormField({ label, field, form, setForm, type = "text",
                     required = false, placeholder = "", disabled = false }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      <input
        type={type}
        value={form[field]}
        onChange={e => setForm({ ...form, [field]: e.target.value })}
        placeholder={placeholder}
        required={required}
        disabled={disabled}
        className={`border border-gray-300 rounded-lg p-2 w-full text-sm
          ${disabled ? "bg-gray-100 cursor-not-allowed" : ""}`}
      />
    </div>
  );
}

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center
      justify-center z-50">
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

function ModalActions({ onCancel, submitLabel }) {
  return (
    <div className="flex justify-end gap-2 pt-2">
      <button type="button" onClick={onCancel}
        className="px-4 py-2 rounded-lg bg-gray-200 text-sm">Cancel</button>
      <button type="submit"
        className="px-4 py-2 rounded-lg bg-blue-600 text-white text-sm">
        {submitLabel}
      </button>
    </div>
  );
}