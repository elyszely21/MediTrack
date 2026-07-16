import Layout from "../../widgets/layout/Layout";

export default function Unauthorized() {
  return (
    <Layout>
      <div className="max-w-2xl mx-auto text-center py-12">
        <div className="text-6xl mb-4">⛔</div>
        <h1 className="text-2xl font-bold text-gray-800 mb-2">Unauthorized</h1>
        <p className="text-gray-600">
          You don’t have permission to view this page.
        </p>
      </div>
    </Layout>
  );
}

