import React, { useState } from 'react';

export const LockerTerminal: React.FC = () => {
  // Drop Off state
  const [dropOffCode, setDropOffCode] = useState('');
  const [courierName, setCourierName] = useState('');

  // Pick Up state
  const [pickUpTrackingCode, setPickUpTrackingCode] = useState('');
  const [pickupCode, setPickupCode] = useState('');

  // Status message state
  const [message, setMessage] = useState<string | null>(null);

  const handleDropOff = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await fetch(`http://localhost:8080/api/v1/parcels/${dropOffCode}/dropoff`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ actor: courierName }),
      });
      const text = await response.text();
      setMessage(text || 'Parcel dropped off successfully.');
    } catch (error) {
      setMessage('Failed to execute drop off operation.');
    }
  };

  const handlePickUp = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await fetch(`http://localhost:8080/api/v1/parcels/${pickUpTrackingCode}/pickup`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ pickupCode: pickupCode, actor: courierName }),
      });
      const text = await response.text();
      setMessage(text || 'Parcel picked up successfully.');
    } catch (error) {
      setMessage('Failed to execute pick up operation.');
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-8">
      <h1 className="text-3xl font-bold text-center text-gray-800">Locker Terminal</h1>

      <div className="grid md:grid-cols-2 gap-6">
        {/* Drop Off Section */}
        <div className="p-6 bg-white border border-gray-200 rounded-lg shadow-sm space-y-4">
          <h2 className="text-xl font-semibold text-gray-800 border-b pb-2">Drop Off</h2>
          <form onSubmit={handleDropOff} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Tracking Code</label>
              <input
                type="text"
                name="trackingCode"
                value={dropOffCode}
                onChange={(e) => setDropOffCode(e.target.value)}
                placeholder="Tracking code"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Courier Name</label>
              <input
                type="text"
                name="actor"
                value={courierName}
                onChange={(e) => setCourierName(e.target.value)}
                placeholder="Courier name"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <button
              type="submit"
              className="w-full py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors"
            >
              Drop Off
            </button>
          </form>
        </div>

        {/* Pick Up Section */}
        <div className="p-6 bg-white border border-gray-200 rounded-lg shadow-sm space-y-4">
          <h2 className="text-xl font-semibold text-gray-800 border-b pb-2">Pick Up</h2>
          <form onSubmit={handlePickUp} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Tracking Code</label>
              <input
                type="text"
                name="trackingCode"
                value={pickUpTrackingCode}
                onChange={(e) => setPickUpTrackingCode(e.target.value)}
                placeholder="Tracking code"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Pickup Code</label>
              <input
                type="text"
                name="pickupCode"
                value={pickupCode}
                onChange={(e) => setPickupCode(e.target.value)}
                placeholder="Pickup code"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500"
              />
            </div>
            <button
              type="submit"
              className="w-full py-2 bg-green-600 hover:bg-green-700 text-white font-medium rounded-lg transition-colors"
            >
              Pick Up
            </button>
          </form>
        </div>
      </div>

      {message && (
        <div className="p-4 bg-gray-50 border border-gray-300 rounded-lg text-center">
          <p data-testid="result-message" className="text-gray-800 font-medium">
            {message}
          </p>
        </div>
      )}
    </div>
  );
};
