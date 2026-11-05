import React, { useState, useEffect } from 'react';

interface Parcel {
  trackingCode: string;
  station: string;
  state: string;
  pickupDeadline: string;
}

export const OperatorDashboard: React.FC = () => {
  const [parcels, setParcels] = useState<Parcel[]>([]);

  const fetchParcels = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/v1/operator/parcels');
      if (response.ok) {
        const data = await response.json();
        setParcels(data);
      }
    } catch (error) {
      console.error('Failed to fetch parcels:', error);
    }
  };

  useEffect(() => {
    fetchParcels();
  }, []);

  const handleMarkReturned = async (trackingCode: string) => {
    try {
      const response = await fetch(`http://localhost:8080/api/v1/operator/${trackingCode}/return`, {
        method: 'PUT',
      });
      if (response.ok) {
        fetchParcels();
      }
    } catch (error) {
      console.error(`Failed to mark parcel ${trackingCode} as returned:`, error);
    }
  };

  return (
    <div className="max-w-6xl mx-auto p-6 space-y-6">
      <h1 className="text-3xl font-bold text-gray-800">Operator Dashboard</h1>

      <div className="space-y-4">
        <h2 className="text-xl font-semibold text-gray-700">All Parcels</h2>

        <div className="overflow-x-auto border border-gray-200 rounded-lg shadow-sm">
          <table className="min-w-full divide-y divide-gray-200 bg-white text-sm text-left">
            <thead className="bg-gray-50 text-gray-700 font-semibold uppercase tracking-wider">
              <tr>
                <th className="px-6 py-3">Tracking Code</th>
                <th className="px-6 py-3">Station</th>
                <th className="px-6 py-3">State</th>
                <th className="px-6 py-3">Pickup Deadline</th>
                <th className="px-6 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {parcels.map((p) => (
                <tr key={p.trackingCode} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 font-mono font-medium text-gray-900">{p.trackingCode}</td>
                  <td className="px-6 py-4 text-gray-700">{p.station}</td>
                  <td className="px-6 py-4">
                    <span
                      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                        p.state === 'EXPIRED'
                          ? 'bg-red-100 text-red-800'
                          : p.state === 'RETURNED'
                          ? 'bg-gray-100 text-gray-800'
                          : 'bg-blue-100 text-blue-800'
                      }`}
                    >
                      {p.state}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-gray-700">{p.pickupDeadline}</td>
                  <td className="px-6 py-4">
                    {p.state === 'EXPIRED' && (
                      <button
                        onClick={() => handleMarkReturned(p.trackingCode)}
                        className="px-3 py-1.5 bg-red-600 hover:bg-red-700 text-white text-xs font-medium rounded transition-colors"
                      >
                        Mark Returned
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {parcels.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-6 py-4 text-center text-gray-500">
                    No parcels found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
