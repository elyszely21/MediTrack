import { useState } from "react";
import Layout from "../../widgets/layout/Layout";
import axios from "../../shared/api/axios";

const STATUS_COLORS = {
  UNPAID:        "bg-red-100 text-red-800",
  PARTIALLY_PAID:"bg-yellow-100 text-yellow-800",
  PAID:          "bg-green-100 text-green-800",
};

export default function Billing() {
  const [bills, setBills]                 = useState([]);
  const [loading, setLoading]             = useState(false);
  const [error, setError]                 = useState("");
  const [patientId, setPatientId]         = useState("");
  const [searchedId, setSearchedId]       = useState(null);
  const [showBillForm, setShowBillForm]   = useState(false);
  const [deletTarget, setDeleteTarget]    = useState(null);
  const [paymentTarget, setPaymentTarget] = useState(null);
  const [payments, setPayments]           = useState([]);
  const [showPayments, setShowPayments]   = useState(null);
  const [printBill, setPrintBill]         = useState(null);
  const [printPayments, setPrintPayments] = useState([]);

  const [billForm, setBillForm] = useState({
    patientId: "", appointmentId: "",
    description: "", amount: "", billDate: ""
  });
  const [paymentForm, setPaymentForm] = useState({
    amountPaid: "", paymentMethod: "CASH",
    receivedBy: "", notes: ""
  });

  const fetchBills = async (pid) => {
    if (!pid) return;
    setLoading(true);
    setError("");
    try {
      const res = await axios.get(`/bills/patient/${pid}`);
      setBills(res.data);
      setSearchedId(pid);
    } catch {
      setError("Failed to load bills.");
    } finally {
      setLoading(false);
    }
  };

  const fetchPayments = async (billId) => {
    const res = await axios.get(`/bills/${billId}/payments`);
    setPayments(res.data);
    setShowPayments(billId);
  };

  const handleCreateBill = async (e) => {
    e.preventDefault();
    try {
      await axios.post("/bills", {
        ...billForm,
        patientId:     Number(billForm.patientId),
        appointmentId: billForm.appointmentId
          ? Number(billForm.appointmentId) : null,
        amount:        Number(billForm.amount),
        billDate:      billForm.billDate
          || new Date().toISOString().split("T")[0],
      });
      setShowBillForm(false);
      setBillForm({ patientId:"", appointmentId:"",
        description:"", amount:"", billDate:"" });
      fetchBills(searchedId);
    } catch (e) {
      alert(e.response?.data?.message || "Failed to create bill.");
    }
  };

  const handleDeleteBill = async () => {
    try {
      await axios.delete(`/bills/${deletTarget.id}`);
      setDeleteTarget(null);
      fetchBills(searchedId);
    } catch {
      alert("Failed to delete bill.");
    }
  };

  const handleRecordPayment = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`/bills/${paymentTarget.id}/payments`, {
        amountPaid:    Number(paymentForm.amountPaid),
        paymentMethod: paymentForm.paymentMethod,
        receivedBy:    paymentForm.receivedBy || null,
        notes:         paymentForm.notes      || null,
      });
      setPaymentTarget(null);
      setPaymentForm({ amountPaid:"", paymentMethod:"CASH",
        receivedBy:"", notes:"" });
      fetchBills(searchedId);
    } catch (e) {
      alert(e.response?.data?.message || "Failed to record payment.");
    }
  };

  const handlePrintReceipt = async (bill) => {
    try {
      const res = await axios.get(`/bills/${bill.id}/payments`);
      setPrintBill(bill);
      setPrintPayments(res.data);
      setTimeout(() => window.print(), 300);
    } catch {
      alert("Failed to load payment data for printing.");
    }
  };

  return (
    <Layout>
      {/* ── Print styles ─────────────────────────────────────────────────── */}
      <style>{`
        @media print {
          body * { visibility: hidden; }
          #receipt-print-area, #receipt-print-area * {
            visibility: visible;
          }
          #receipt-print-area {
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

      {/* ── Receipt print area ────────────────────────────────────────────── */}
      {printBill && (
        <div id="receipt-print-area" className="hidden print:block">
          {/* Header */}
          <div style={{ textAlign: "center", borderBottom: "2px solid #000",
            paddingBottom: 16, marginBottom: 20 }}>
            <h1 style={{ fontSize: 24, fontWeight: "bold", margin: 0 }}>
              MediTrack
            </h1>
            <p style={{ margin: "4px 0 0", color: "#555" }}>
              Clinic Records Management System
            </p>
            <p style={{ margin: "4px 0 0", fontSize: 12, color: "#777" }}>
              Official Receipt
            </p>
          </div>

          {/* Receipt number + date */}
          <div style={{ display: "flex", justifyContent: "space-between",
            marginBottom: 20 }}>
            <div>
              <p style={{ margin: 0, color: "#555" }}>Receipt No:</p>
              <p style={{ margin: "2px 0 0", fontWeight: "bold" }}>
                RCPT-{String(printBill.id).padStart(5, "0")}
              </p>
            </div>
            <div style={{ textAlign: "right" }}>
              <p style={{ margin: 0, color: "#555" }}>Date Printed:</p>
              <p style={{ margin: "2px 0 0" }}>
                {new Date().toLocaleDateString("en-PH", {
                  year: "numeric", month: "long", day: "numeric"
                })}
              </p>
            </div>
          </div>

          {/* Bill details */}
          <div style={{ border: "1px solid #ddd", borderRadius: 6,
            padding: 16, marginBottom: 20 }}>
            <h3 style={{ margin: "0 0 12px", fontSize: 14,
              borderBottom: "1px solid #eee", paddingBottom: 8 }}>
              Bill Details
            </h3>
            <table style={{ width: "100%", borderCollapse: "collapse" }}>
              <tbody>
                {[
                  ["Patient ID",   printBill.patientId],
                  ["Description",  printBill.description],
                  ["Bill Date",    printBill.billDate],
                  ["Total Amount", `₱${Number(printBill.amount).toFixed(2)}`],
                  ["Amount Paid",  `₱${(Number(printBill.amount)
                    - Number(printBill.balance)).toFixed(2)}`],
                  ["Balance",      `₱${Number(printBill.balance).toFixed(2)}`],
                  ["Status",       printBill.status],
                ].map(([label, val]) => (
                  <tr key={label}>
                    <td style={{ padding: "5px 8px 5px 0", color: "#555",
                      width: 140 }}>{label}:</td>
                    <td style={{ padding: "5px 0", fontWeight: "500" }}>
                      {val}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Payment history */}
          {printPayments.length > 0 && (
            <div style={{ border: "1px solid #ddd", borderRadius: 6,
              padding: 16, marginBottom: 20 }}>
              <h3 style={{ margin: "0 0 12px", fontSize: 14,
                borderBottom: "1px solid #eee", paddingBottom: 8 }}>
                Payment History
              </h3>
              <table style={{ width: "100%", borderCollapse: "collapse",
                fontSize: 12 }}>
                <thead>
                  <tr style={{ background: "#f5f5f5" }}>
                    {["#","Amount","Method","Received By","Date"].map(h => (
                      <th key={h} style={{ padding: "6px 8px", textAlign: "left",
                        border: "1px solid #ddd" }}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {printPayments.map((p, i) => (
                    <tr key={p.id}>
                      <td style={{ padding: "6px 8px",
                        border: "1px solid #ddd" }}>{i + 1}</td>
                      <td style={{ padding: "6px 8px",
                        border: "1px solid #ddd", fontWeight: "bold" }}>
                        ₱{Number(p.amountPaid).toFixed(2)}
                      </td>
                      <td style={{ padding: "6px 8px",
                        border: "1px solid #ddd" }}>
                        {p.paymentMethod || "—"}
                      </td>
                      <td style={{ padding: "6px 8px",
                        border: "1px solid #ddd" }}>
                        {p.receivedBy || "—"}
                      </td>
                      <td style={{ padding: "6px 8px",
                        border: "1px solid #ddd" }}>
                        {p.paymentDate
                          ? new Date(p.paymentDate).toLocaleDateString()
                          : "—"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Summary box */}
          <div style={{ background: "#f9f9f9", border: "1px solid #ddd",
            borderRadius: 6, padding: 16, marginBottom: 20,
            display: "flex", justifyContent: "space-between",
            alignItems: "center" }}>
            <div>
              <p style={{ margin: 0, fontSize: 12, color: "#555" }}>
                Payment Status
              </p>
              <p style={{ margin: "4px 0 0", fontWeight: "bold",
                fontSize: 16,
                color: printBill.status === "PAID" ? "#16a34a" : "#dc2626" }}>
                {printBill.status}
              </p>
            </div>
            <div style={{ textAlign: "right" }}>
              <p style={{ margin: 0, fontSize: 12, color: "#555" }}>
                Remaining Balance
              </p>
              <p style={{ margin: "4px 0 0", fontWeight: "bold",
                fontSize: 18,
                color: Number(printBill.balance) > 0 ? "#dc2626" : "#16a34a" }}>
                ₱{Number(printBill.balance).toFixed(2)}
              </p>
            </div>
          </div>

          {/* Footer */}
          <div style={{ marginTop: 40, borderTop: "1px solid #000",
            paddingTop: 12, display: "flex",
            justifyContent: "space-between" }}>
            <div>
              <p style={{ margin: 0, fontSize: 11, color: "#555" }}>
                This is a computer-generated receipt.
              </p>
              <p style={{ margin: "4px 0 0", fontSize: 11, color: "#555" }}>
                No signature required.
              </p>
            </div>
            <div style={{ textAlign: "right" }}>
              <p style={{ margin: 0 }}>MediTrack Clinic Records System</p>
              <p style={{ margin: "4px 0 0", fontSize: 11, color: "#555" }}>
                Printed: {new Date().toLocaleString()}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* ── Main content ──────────────────────────────────────────────────── */}
      <div className="p-6 max-w-5xl mx-auto no-print">
        <h1 className="text-2xl font-bold mb-6">Billing</h1>

        {/* Search bar */}
        <div className="flex gap-2 mb-6">
          <input
            placeholder="Enter Patient ID"
            value={patientId}
            onChange={e => setPatientId(e.target.value)}
            onKeyDown={e => e.key === "Enter" && fetchBills(patientId)}
            className="border border-gray-300 rounded p-2 w-48 text-sm"
          />
          <button onClick={() => fetchBills(patientId)}
            className="bg-blue-600 text-white px-4 py-2 rounded text-sm">
            Search
          </button>
          {searchedId && (
            <button onClick={() => {
              setBillForm({ ...billForm, patientId: searchedId });
              setShowBillForm(true);
            }}
              className="bg-green-600 text-white px-4 py-2 rounded
                text-sm ml-auto">
              + Create Bill
            </button>
          )}
        </div>

        {loading && <p className="text-gray-500">Loading...</p>}
        {error   && <p className="text-red-500">{error}</p>}

        {!loading && bills.length === 0 && searchedId && (
          <p className="text-gray-400 text-center mt-10">
            No bills found for Patient #{searchedId}
          </p>
        )}

        {/* Bills list */}
        <div className="space-y-4">
          {bills.map(b => (
            <div key={b.id}
              className="border border-gray-200 rounded-lg p-4
                shadow-sm bg-white">
              <div className="flex justify-between items-start mb-3">
                <div>
                  <p className="font-semibold text-gray-800">
                    {b.description}
                  </p>
                  <p className="text-xs text-gray-400 mt-0.5">
                    Bill Date: {b.billDate}
                  </p>
                </div>
                <span className={`px-2 py-0.5 rounded text-xs font-medium
                  ${STATUS_COLORS[b.status]
                  || "bg-gray-100 text-gray-700"}`}>
                  {b.status}
                </span>
              </div>

              {/* Amount summary */}
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
                    Number(b.balance) > 0
                      ? "text-red-600" : "text-green-600"}`}>
                    ₱{Number(b.balance).toFixed(2)}
                  </p>
                </div>
              </div>

              {/* Actions */}
              <div className="flex gap-3 text-sm flex-wrap">
                {b.status !== "PAID" && (
                  <button onClick={() => setPaymentTarget(b)}
                    className="bg-blue-600 text-white px-3 py-1.5
                      rounded text-xs">
                    Record Payment
                  </button>
                )}
                <button onClick={() => fetchPayments(b.id)}
                  className="border border-gray-300 px-3 py-1.5
                    rounded text-xs">
                  View Payments
                </button>
                <button onClick={() => handlePrintReceipt(b)}
                  className="border border-green-400 text-green-700
                    px-3 py-1.5 rounded text-xs hover:bg-green-50
                    flex items-center gap-1">
                  🖨️ Print Receipt
                </button>
                <button onClick={() => setDeleteTarget(b)}
                  className="text-red-500 hover:underline text-xs ml-auto">
                  Delete
                </button>
              </div>

              {/* Payment history inline */}
              {showPayments === b.id && (
                <div className="mt-3 border-t pt-3">
                  <div className="flex justify-between items-center mb-2">
                    <p className="text-sm font-medium text-gray-600">
                      Payment History
                    </p>
                    <button onClick={() => setShowPayments(null)}
                      className="text-xs text-gray-400 hover:text-gray-600">
                      Hide
                    </button>
                  </div>
                  {payments.length === 0 ? (
                    <p className="text-xs text-gray-400">
                      No payments recorded yet.
                    </p>
                  ) : (
                    <table className="w-full text-xs border-collapse">
                      <thead>
                        <tr className="bg-gray-50">
                          {["Amount","Method","Received By",
                            "Notes","Date"].map(h => (
                            <th key={h} className="border border-gray-200
                              p-1.5 text-left text-gray-600">{h}</th>
                          ))}
                        </tr>
                      </thead>
                      <tbody>
                        {payments.map(p => (
                          <tr key={p.id}>
                            <td className="border border-gray-200 p-1.5
                              font-medium">
                              ₱{Number(p.amountPaid).toFixed(2)}
                            </td>
                            <td className="border border-gray-200 p-1.5">
                              {p.paymentMethod || "—"}
                            </td>
                            <td className="border border-gray-200 p-1.5">
                              {p.receivedBy || "—"}
                            </td>
                            <td className="border border-gray-200 p-1.5">
                              {p.notes || "—"}
                            </td>
                            <td className="border border-gray-200 p-1.5">
                              {p.paymentDate
                                ? new Date(p.paymentDate)
                                    .toLocaleDateString()
                                : "—"}
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

        {/* Create Bill modal */}
        {showBillForm && (
          <Modal title="Create Bill"
            onClose={() => setShowBillForm(false)}>
            <form onSubmit={handleCreateBill} className="space-y-3">
              <FormField label="Patient ID *" field="patientId"
                form={billForm} setForm={setBillForm} required />
              <FormField label="Appointment ID" field="appointmentId"
                form={billForm} setForm={setBillForm} />
              <FormField label="Description *" field="description"
                form={billForm} setForm={setBillForm} required />
              <FormField label="Amount *" field="amount"
                form={billForm} setForm={setBillForm}
                type="number" required placeholder="e.g. 500.00" />
              <FormField label="Bill Date" field="billDate"
                form={billForm} setForm={setBillForm} type="date" />
              <ModalActions onCancel={() => setShowBillForm(false)}
                submitLabel="Create Bill" />
            </form>
          </Modal>
        )}

        {/* Record Payment modal */}
        {paymentTarget && (
          <Modal
            title={`Record Payment — ${paymentTarget.description}`}
            onClose={() => setPaymentTarget(null)}>
            <div className="bg-gray-50 rounded p-3 mb-4 text-sm">
              <p>Total:{" "}
                <strong>₱{Number(paymentTarget.amount).toFixed(2)}</strong>
              </p>
              <p>Remaining Balance:{" "}
                <strong className="text-red-600">
                  ₱{Number(paymentTarget.balance).toFixed(2)}
                </strong>
              </p>
            </div>
            <form onSubmit={handleRecordPayment} className="space-y-3">
              <FormField label="Amount Paid *" field="amountPaid"
                form={paymentForm} setForm={setPaymentForm}
                type="number" required
                placeholder="Enter payment amount" />
              <div>
                <label className="block text-sm font-medium
                  text-gray-700 mb-1">Payment Method</label>
                <select value={paymentForm.paymentMethod}
                  onChange={e => setPaymentForm({
                    ...paymentForm, paymentMethod: e.target.value })}
                  className="border border-gray-300 rounded p-2
                    w-full text-sm">
                  {["CASH","CARD","GCASH","OTHERS"].map(m => (
                    <option key={m}>{m}</option>
                  ))}
                </select>
              </div>
              <FormField label="Received By" field="receivedBy"
                form={paymentForm} setForm={setPaymentForm}
                placeholder="Staff name" required />
              <FormField label="Notes" field="notes"
                form={paymentForm} setForm={setPaymentForm} />
              <ModalActions onCancel={() => setPaymentTarget(null)}
                submitLabel="Record Payment" />
            </form>
          </Modal>
        )}

        {/* Delete confirmation */}
        {deletTarget && (
          <Modal title="Confirm Delete"
            onClose={() => setDeleteTarget(null)}>
            <p className="text-gray-600 mb-4">
              Delete bill for{" "}
              <strong>{deletTarget.description}</strong>?
              All associated payments will also be removed.
            </p>
            <div className="flex justify-end gap-2">
              <button onClick={() => setDeleteTarget(null)}
                className="px-4 py-2 rounded bg-gray-200 text-sm">
                Cancel
              </button>
              <button onClick={handleDeleteBill}
                className="px-4 py-2 rounded bg-red-600
                  text-white text-sm">
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
                     required = false, placeholder = "",
                     multiline = false }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        {label}
      </label>
      {multiline ? (
        <textarea rows={3} value={form[field]}
          placeholder={placeholder} required={required}
          onChange={e => setForm({ ...form, [field]: e.target.value })}
          className="border border-gray-300 rounded p-2 w-full text-sm" />
      ) : (
        <input type={type} value={form[field]}
          placeholder={placeholder} required={required}
          onChange={e => setForm({ ...form, [field]: e.target.value })}
          className="border border-gray-300 rounded p-2 w-full text-sm" />
      )}
    </div>
  );
}

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-40 flex
      items-center justify-center z-50 p-4">
      <div role="dialog" aria-modal="true" aria-label={title}
        className="bg-white rounded-lg shadow-xl w-full max-w-lg mx-4
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