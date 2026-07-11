import { useEffect, useState } from "react";
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

  const openAdd = () => { resetForm(); setEditTarget(null); setShowForm(true); };

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
      medicalRecordId: form.medicalRecordId ? Number(form.medicalRecordId) : null,
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

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <h1 className="text-2xl font-bold mb-6">Prescriptions</h1>

      {/* Search bar */}
      <div className="flex gap-2 mb-6">
        <input
          placeholder="Enter Patient ID"
          value={patientId}
          onChange={e => setPatientId(e.target.value)}
          onKeyDown={e => e.key === "Enter" && fetchPrescriptions(patientId)}
          className="border border-gray-300 rounded p-2 w-48 text-sm"
        />
        <button
          onClick={() => fetchPrescriptions(patientId)}
          className="bg-blue-600 text-white px-4 py-2 rounded text-sm"
        >
          Search
        </button>
        {searchedId && (
          <button
            onClick={openAdd}
            className="bg-green-600 text-white px-4 py-2 rounded text-sm ml-auto"
          >
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
          <table className="w-full border-collapse border border-gray-200 text-sm">
            <thead className="bg-gray-50">
              <tr>
                {["Medication","Dosage","Frequency","Duration",
                  "Prescribed By","Date","Status","Actions"].map(h => (
                  <th key={h}
                    className="border border-gray-200 p-2 text-left font-medium text-gray-600">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {prescriptions.map(p => (
                <tr key={p.id} className="hover:bg-gray-50">
                  <td className="border border-gray-200 p-2 font-medium">{p.medication}</td>
                  <td className="border border-gray-200 p-2">{p.dosage   || "—"}</td>
                  <td className="border border-gray-200 p-2">{p.frequency || "—"}</td>
                  <td className="border border-gray-200 p-2">{p.duration  || "—"}</td>
                  <td className="border border-gray-200 p-2">{p.prescribedBy}</td>
                  <td className="border border-gray-200 p-2">{p.prescribedDate}</td>
                  <td className="border border-gray-200 p-2">
                    <span className={`px-2 py-0.5 rounded text-xs font-medium
                      ${STATUS_COLORS[p.status] || "bg-gray-100 text-gray-700"}`}>
                      {p.status}
                    </span>
                  </td>
                  <td className="border border-gray-200 p-2">
                    <div className="flex gap-2">
                      <button onClick={() => openEdit(p)}
                        className="text-blue-600 hover:underline text-xs">Edit</button>
                      <button onClick={() => setDeleteTarget(p)}
                        className="text-red-500 hover:underline text-xs">Delete</button>
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
            <FormField label="Patient ID *"       field="patientId"
              form={form} setForm={setForm} required />
            <FormField label="Medical Record ID"  field="medicalRecordId"
              form={form} setForm={setForm} />
            <FormField label="Medication *"       field="medication"
              form={form} setForm={setForm} required />
            <div className="grid grid-cols-2 gap-3">
              <FormField label="Dosage"     field="dosage"
                form={form} setForm={setForm} />
              <FormField label="Frequency"  field="frequency"
                form={form} setForm={setForm} placeholder="e.g. Twice daily" />
              <FormField label="Duration"   field="duration"
                form={form} setForm={setForm} placeholder="e.g. 7 days" />
              <FormField label="Prescribed Date *" field="prescribedDate"
                form={form} setForm={setForm} type="date" required />
            </div>
            <FormField label="Prescribed By *" field="prescribedBy"
              form={form} setForm={setForm} required />
            <FormField label="Instructions" field="instructions"
              form={form} setForm={setForm} multiline />
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
              <select value={form.status}
                onChange={e => setForm({ ...form, status: e.target.value })}
                className="border border-gray-300 rounded p-2 w-full text-sm">
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
        <Modal title="Confirm Delete" onClose={() => setDeleteTarget(null)}>
          <p className="text-gray-600 mb-4">
            Delete prescription for <strong>{deleteTarget.medication}</strong>?
          </p>
          <div className="flex justify-end gap-2">
            <button onClick={() => setDeleteTarget(null)}
              className="px-4 py-2 rounded bg-gray-200 text-sm">Cancel</button>
            <button onClick={handleDelete}
              className="px-4 py-2 rounded bg-red-600 text-white text-sm">Delete</button>
          </div>
        </Modal>
      )}
    </div>
  );
}