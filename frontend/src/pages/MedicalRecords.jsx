import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import Layout from "../components/Layout";
import axios from "../api/axios";

export default function MedicalRecords() {
  const [searchParams]              = useSearchParams();
  const [patients, setPatients]     = useState([]);
  const [patientId, setPatientId]   = useState(searchParams.get("patientId") || "");
  const [records, setRecords]       = useState([]);
  const [loading, setLoading]       = useState(false);
  const [error, setError]           = useState("");
  const [showForm, setShowForm]     = useState(false);
  const [successMsg, setSuccessMsg] = useState("");
  const [form, setForm] = useState({
    diagnosis: "", treatment: "", prescription: "",
    notes: "", visitDate: ""
  });

  useEffect(() => {
    axios.get("/patients").then(r => setPatients(r.data)).catch(() => {});
  }, []);

  const loadRecords = async (id) => {
    if (!id) { setRecords([]); return; }
    setLoading(true);
    try {
      const res = await axios.get(`/records/patient/${id}`);
      setRecords(res.data);
    } catch {
      setError("Failed to load records.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadRecords(patientId); }, [patientId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (!patientId) { setError("Select a patient first."); return; }
    try {
      await axios.post("/records", { ...form, patientId: Number(patientId) });
      setForm({ diagnosis:"", treatment:"", prescription:"", notes:"", visitDate:"" });
      setShowForm(false);
      setSuccessMsg("Record added successfully.");
      setTimeout(() => setSuccessMsg(""), 3000);
      loadRecords(patientId);
    } catch (e) {
      setError(e.response?.data?.message || "Failed to create record.");
    }
  };

  const selectedPatient = patients.find(p => String(p.id) === String(patientId));

  return (
    <Layout>
      <div className="flex justify-between items-start mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Medical Records</h1>
          <p className="text-gray-500 text-sm mt-1">
            View and manage patient clinic visit records
          </p>
        </div>
        {patientId && (
          <button onClick={() => setShowForm(true)}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg
              text-sm hover:bg-blue-700">
            + Add Record
          </button>
        )}
      </div>

      {/* Patient selector */}
      <div className="bg-white border border-gray-200 rounded-xl p-4
        mb-6 shadow-sm">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Select Patient
        </label>
        <select value={patientId}
          onChange={e => setPatientId(e.target.value)}
          className="border border-gray-300 rounded-lg p-2 w-full
            text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
          <option value="">— Select a patient —</option>
          {patients.map(p => (
            <option key={p.id} value={p.id}>
              {p.patientNumber} — {p.firstName} {p.lastName}
            </option>
          ))}
        </select>
        {selectedPatient && (
          <div className="mt-3 flex items-center gap-3">
            <div className="w-8 h-8 bg-blue-100 rounded-full flex
              items-center justify-center text-blue-600 font-bold text-sm">
              {selectedPatient.firstName?.charAt(0)}
            </div>
            <div>
              <p className="font-medium text-gray-800 text-sm">
                {selectedPatient.firstName} {selectedPatient.lastName}
              </p>
              <p className="text-xs text-gray-500">
                {selectedPatient.patientNumber} • {selectedPatient.gender} •{" "}
                {selectedPatient.contactNumber}
              </p>
            </div>
          </div>
        )}
      </div>

      {successMsg && (
        <div className="bg-green-50 border border-green-200 text-green-700
          rounded-lg p-3 mb-4 text-sm">✅ {successMsg}</div>
      )}
      {error && (
        <p className="text-red-500 text-sm mb-4">{error}</p>
      )}

      {!patientId ? (
        <div className="text-center py-16 text-gray-400">
          <p className="text-5xl mb-3">📋</p>
          <p className="font-medium text-gray-500">
            Select a patient to view their records
          </p>
        </div>
      ) : loading ? (
        <div className="space-y-3">
          {[1,2,3].map(i => (
            <div key={i}
              className="h-24 bg-gray-200 animate-pulse rounded-xl" />
          ))}
        </div>
      ) : records.length === 0 ? (
        <div className="text-center py-16 text-gray-400">
          <p className="text-5xl mb-3">📭</p>
          <p className="font-medium text-gray-500">
            No records found for this patient
          </p>
          <button onClick={() => setShowForm(true)}
            className="mt-4 bg-blue-600 text-white px-4 py-2
              rounded-lg text-sm">
            Add First Record
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          {records.map(r => (
            <div key={r.id}
              className="bg-white border border-gray-200 rounded-xl
                p-5 shadow-sm">
              <div className="flex justify-between items-start mb-3">
                <div>
                  <p className="font-semibold text-gray-800 text-lg">
                    {r.diagnosis || "No diagnosis"}
                  </p>
                  <p className="text-xs text-blue-600 font-medium mt-0.5">
                    📅 Visit Date: {r.visitDate || "—"}
                  </p>
                </div>
                <span className="text-xs text-gray-400">
                  Record #{r.id}
                </span>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-3
                text-sm">
                {r.treatment && (
                  <div className="bg-gray-50 rounded-lg p-3">
                    <p className="text-xs text-gray-500 mb-1">Treatment</p>
                    <p className="text-gray-800">{r.treatment}</p>
                  </div>
                )}
                {r.prescription && (
                  <div className="bg-blue-50 rounded-lg p-3">
                    <p className="text-xs text-blue-500 mb-1">Prescription</p>
                    <p className="text-gray-800">{r.prescription}</p>
                  </div>
                )}
                {r.notes && (
                  <div className="md:col-span-2 bg-yellow-50 rounded-lg p-3">
                    <p className="text-xs text-yellow-600 mb-1">Notes</p>
                    <p className="text-gray-700 italic">{r.notes}</p>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add Record Modal */}
      {showForm && (
        <Modal title="Add Medical Record" onClose={() => setShowForm(false)}>
          <form onSubmit={handleSubmit} className="space-y-3">
            <FormField label="Visit Date *" field="visitDate"
              form={form} setForm={setForm} type="date" required />
            <FormField label="Diagnosis *" field="diagnosis"
              form={form} setForm={setForm} required />
            <FormField label="Treatment" field="treatment"
              form={form} setForm={setForm} multiline />
            <FormField label="Prescription" field="prescription"
              form={form} setForm={setForm} multiline />
            <FormField label="Notes" field="notes"
              form={form} setForm={setForm} multiline />
            <div className="flex justify-end gap-2 pt-2">
              <button type="button" onClick={() => setShowForm(false)}
                className="px-4 py-2 rounded-lg bg-gray-200 text-sm">
                Cancel
              </button>
              <button type="submit"
                className="px-4 py-2 rounded-lg bg-blue-600
                  text-white text-sm">
                Save Record
              </button>
            </div>
          </form>
        </Modal>
      )}
    </Layout>
  );
}

function FormField({ label, field, form, setForm, type="text",
                     required=false, multiline=false }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        {label}
      </label>
      {multiline ? (
        <textarea rows={3} value={form[field]} required={required}
          onChange={e => setForm({ ...form, [field]: e.target.value })}
          className="border border-gray-300 rounded-lg p-2 w-full
            text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
      ) : (
        <input type={type} value={form[field]} required={required}
          onChange={e => setForm({ ...form, [field]: e.target.value })}
          className="border border-gray-300 rounded-lg p-2 w-full
            text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
      )}
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