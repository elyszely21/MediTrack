import { useEffect, useState } from "react";
import axios from "../api/axios";

export default function MedicalRecords() {
  const [records, setRecords] = useState([]);
  const [patientId, setPatientId] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    patientId:"", diagnosis:"", treatment:"",
    prescription:"", notes:"", visitDate:""
  });

  const fetchRecords = async () => {
    if (!patientId) return;
    const res = await axios.get(`/records/patient/${patientId}`);
    setRecords(res.data);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    await axios.post("/records", { ...form, patientId: Number(form.patientId) });
    setShowForm(false);
    fetchRecords();
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">Medical Records</h1>

      <div className="flex gap-2 mb-4">
        <input placeholder="Enter Patient ID" value={patientId}
          onChange={e => setPatientId(e.target.value)}
          className="border border-gray-300 p-2 rounded w-48" />
        <button onClick={fetchRecords}
          className="bg-blue-600 text-white px-4 py-2 rounded">Search</button>
        <button onClick={() => setShowForm(true)}
          className="bg-green-600 text-white px-4 py-2 rounded">+ Add Record</button>
      </div>

      <div className="space-y-4">
        {records.map(r => (
          <div key={r.id} className="border border-gray-200 rounded p-4 shadow-sm">
            <div className="flex justify-between mb-2">
              <span className="font-semibold text-lg">{r.diagnosis}</span>
              <span className="text-gray-500 text-sm">{r.visitDate}</span>
            </div>
            <p><span className="font-medium">Treatment:</span> {r.treatment}</p>
            <p><span className="font-medium text-blue-600">Rx:</span> {r.prescription}</p>
            {r.notes && <p className="text-gray-500 italic">{r.notes}</p>}
          </div>
        ))}
      </div>

      {showForm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white p-6 rounded shadow-lg w-[500px]">
            <h2 className="text-xl font-bold mb-4">Add Medical Record</h2>
            <form onSubmit={handleSubmit} className="space-y-3">
              {[
                ["patientId","Patient ID"],["diagnosis","Diagnosis"],
                ["treatment","Treatment"],["prescription","Prescription"],
                ["notes","Notes"],["visitDate","Visit Date (yyyy-MM-dd)"]
              ].map(([field, label]) => (
                <input key={field} placeholder={label} value={form[field]}
                  onChange={e => setForm({...form, [field]: e.target.value})}
                  className="border p-2 w-full rounded" />
              ))}
              <div className="flex gap-2">
                <button type="submit" className="bg-green-600 text-white px-4 py-2 rounded">Save</button>
                <button type="button" onClick={() => setShowForm(false)}
                  className="bg-gray-300 px-4 py-2 rounded">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}