import { useEffect, useState } from "react";
import Layout from "../../widgets/layout/Layout";
import axios from "../../shared/api/axios";

const STATUS_COLORS = {
  UNPAID:        "bg-red-100 text-red-800",
  PARTIALLY_PAID:"bg-yellow-100 text-yellow-800",
  PAID:          "bg-green-100 text-green-800",
};

export default function PatientBilling() {
  const [bills, setBills]         = useState([]);
  const [payments, setPayments]   = useState([]);
  const [loading, setLoading]     = useState(false);
  const [error, setError]         = useState("");
  const [expandedBill, setExpandedBill] = useState(null);

  const fetchBilling = async () => {
    setLoading(true);
    setError("");
    try {
      const res = await axios.get("/patient-portal/billing");
      setBills(res.data.bills || []);
      setPayments(res.data.payments || []);
    } catch {
      setError("Failed to load billing information.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBilling();
  }, []);

  const getPaymentsForBill = (billId) =>
    payments.filter(p => p.billId === billId);

  return (
    <Layout>
      <div className="p-6 max-w-5xl mx-auto">
        <h1 className="text-2xl font-bold mb-6">My Billing</h1>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 rounded-lg p-3 mb-4 text-sm">
            {error}
            <button onClick={fetchBilling} className="underline ml-3">Retry</button>
          </div>
        )}

        {loading ? (
          <div className="space-y-3">
            {[1,2,3].map(i => (
              <div key={i} className="h-24 bg-gray-200 animate-pulse rounded-xl" />
            ))}
          </div>
        ) : bills.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <p className="text-5xl mb-3">🧾</p>
            <p className="font-medium text-gray-500">No billing records found</p>
          </div>
        ) : (
          <div className="space-y-4">
            {bills.map(b => (
              <div key={b.id}
                className="border border-gray-200 rounded-lg p-4 shadow-sm bg-white">
                <div className="flex justify-between items-start mb-3">
                  <div>
                    <p className="font-semibold text-gray-800">{b.description}</p>
                    <p className="text-xs text-gray-400 mt-0.5">Bill Date: {b.billDate}</p>
                  </div>
                  <span className={`px-2 py-0.5 rounded text-xs font-medium
                    ${STATUS_COLORS[b.status] || "bg-gray-100 text-gray-700"}`}>
                    {b.status}
                  </span>
                </div>

                <div className="grid grid-cols-3 gap-4 mb-3 text-sm">
                  <div className="bg-gray-50 rounded p-3 text-center">
                    <p className="text-gray-500 text-xs">Total Amount</p>
                    <p className="font-bold text-gray-800">
                      ₱{Number(b.amount).toFixed(2)}
                    </p>
                  </div>
                  <div className="bg-gray-50 rounded p-3 text-center">
                    <p className="text-gray-500 text-xs">Amount Paid</p>
                    <p className="font-bold text-green-600">
                      ₱{(Number(b.amount) - Number(b.balance)).toFixed(2)}
                    </p>
                  </div>
                  <div className="bg-gray-50 rounded p-3 text-center">
                    <p className="text-gray-500 text-xs">Balance</p>
                    <p className={`font-bold ${
                      Number(b.balance) > 0 ? "text-red-600" : "text-green-600"}`}>
                      ₱{Number(b.balance).toFixed(2)}
                    </p>
                  </div>
                </div>

                <div className="flex gap-3 text-sm">
                  <button onClick={() => setExpandedBill(expandedBill === b.id ? null : b.id)}
                    className="border border-gray-300 px-3 py-1.5 rounded text-xs">
                    {expandedBill === b.id ? "Hide" : "View"} Payments
                  </button>
                </div>

                {expandedBill === b.id && (
                  <div className="mt-3 border-t pt-3">
                    <p className="text-sm font-medium text-gray-600 mb-2">Payment History</p>
                    {getPaymentsForBill(b.id).length === 0 ? (
                      <p className="text-xs text-gray-400">No payments recorded yet.</p>
                    ) : (
                      <table className="w-full text-xs border-collapse">
                        <thead>
                          <tr className="bg-gray-50">
                            {["Amount","Method","Received By","Date"].map(h => (
                              <th key={h} className="border border-gray-200 p-1.5 text-left text-gray-600">{h}</th>
                            ))}
                          </tr>
                        </thead>
                        <tbody>
                          {getPaymentsForBill(b.id).map(p => (
                            <tr key={p.id}>
                              <td className="border border-gray-200 p-1.5 font-medium">
                                ₱{Number(p.amountPaid).toFixed(2)}
                              </td>
                              <td className="border border-gray-200 p-1.5">{p.paymentMethod || "—"}</td>
                              <td className="border border-gray-200 p-1.5">{p.receivedBy || "—"}</td>
                              <td className="border border-gray-200 p-1.5">
                                {p.paymentDate ? new Date(p.paymentDate).toLocaleDateString() : "—"}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </Layout>
  );
}
