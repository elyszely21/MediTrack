import { useEffect, useState } from "react";
import axios from "../api/axios";

export default function Patients() {
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    firstName: "", lastName: "", patientNumber: "",
    gender: "", birthDate: "", contactNumber: "",
    emergencyContact: "", address: ""
  });

  const fetchPatients = async () => {
    try {
      const res = await axios.get("/patients");
      setPatients(res.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchPatients(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    await axios.post("/patients", form);
    setShowForm(false);
    setForm({ firstName:"",lastName:"",patientNumber:"",gender:"",birthDate:"",contactNumber:"",emergencyContact:"",address:"" });
    fetchPatients();
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">Patients</h1>
        <button onClick={() => setShowForm(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded">
          + Add Patient
        </button>
      </div>

      {loading ? <p>Loading...</p> : (
        <table className="w-full border-collapse border border-gray-300">
          <thead className="bg-gray-100">
            <tr>
              {["Patient No","Name","Gender","Birth Date","Contact"].map(h => (
                <th key={h} className="border border-gray-300 p-2 text-left">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {patients.map(p => (
              <tr key={p.id} className="hover:bg-gray-50">
                <td className="border border-gray-300 p-2">{p.patientNumber}</td>
                <td className="border border-gray-300 p-2">{p.firstName} {p.lastName}</td>
                <td className="border border-gray-300 p-2">{p.gender}</td>
                <td className="border border-gray-300 p-2">{p.birthDate}</td>
                <td className="border border-gray-300 p-2">{p.contactNumber}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {showForm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white p-6 rounded shadow-lg w-[500px]">
            <h2 className="text-xl font-bold mb-4">Add Patient</h2>
            <form onSubmit={handleSubmit} className="space-y-3">
              {[
                ["firstName","First Name"],["lastName","Last Name"],
                ["patientNumber","Patient Number"],["gender","Gender"],
                ["birthDate","Birth Date (yyyy-MM-dd)"],["contactNumber","Contact Number"],
                ["emergencyContact","Emergency Contact"],["address","Address"]
              ].map(([field, label]) => (
                <input key={field} placeholder={label} value={form[field]}
                  onChange={e => setForm({...form, [field]: e.target.value})}
                  className="border border-gray-300 p-2 w-full rounded" />
              ))}
              <div className="flex gap-2">
                <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded">Save</button>
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