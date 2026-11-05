import React, { useState } from 'react';

export const LockerTerminal: React.FC = () => {
  // Drop Off state
  const [dropOffCode, setDropOffCode] = useState('');
  const [courierName, setCourierName] = useState('');
  const [isDropOffDoorOpen, setIsDropOffDoorOpen] = useState(false);

  // Pick Up state
  const [pickUpTrackingCode, setPickUpTrackingCode] = useState('');
  const [pickupCode, setPickupCode] = useState('');
  const [isPickUpDoorOpen, setIsPickUpDoorOpen] = useState(false);

  // Status message state
  const [message, setMessage] = useState<string | null>(null);

  // --- Drop Off Handlers ---
  const handleOpenDropOffCompartment = (e: React.FormEvent) => {
    e.preventDefault();
    if (!dropOffCode.trim() || !courierName.trim()) return;
    setIsDropOffDoorOpen(true);
    setMessage(`Compartment unlocked for ${dropOffCode}. Insert parcel and close the door.`);
  };

  const handleCloseDropOffDoor = async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/v1/parcels/${dropOffCode}/dropoff`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ actor: courierName }),
      });
      const text = await response.text();

      console.log(
        `[NOTIFICATION LOG] Drop-off confirmed for tracking code: ${dropOffCode} by ${courierName}. Pickup code generated & sent to recipient. Backend response:`,
        text
      );

      let extractedCode = text;
      try {
        const data = JSON.parse(text);
        extractedCode = data.pickupCode || text; 
      } catch (e) {
        // Fallback to raw text if the response isn't JSON
      }

      setMessage(`Drop-off successful. Pickup Code: ${extractedCode}`);
      setIsDropOffDoorOpen(false);
      setDropOffCode('');
      setCourierName('');
    } catch (error) {
      setMessage('Failed to execute drop off operation.');
    }
  };

  // --- Pick Up Handlers ---
  const handleOpenPickUpCompartment = (e: React.FormEvent) => {
    e.preventDefault();
    if (!pickUpTrackingCode.trim() || !pickupCode.trim()) return;
    setIsPickUpDoorOpen(true);
    setMessage(`Compartment unlocked for ${pickUpTrackingCode}. Please retrieve your parcel and close the door.`);
  };

  const handleClosePickUpDoor = async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/v1/parcels/${pickUpTrackingCode}/pickup`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ pickupCode: pickupCode, actor: 'Recipient' }),
      });
      
      const text = await response.text();

      console.log(
        `[NOTIFICATION LOG] Pick-up confirmed for tracking code: ${pickUpTrackingCode}. Compartment is now empty. Backend response:`,
        text
      );

      setMessage('Parcel picked up successfully.');
      setIsPickUpDoorOpen(false);
      setPickUpTrackingCode('');
      setPickupCode('');
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
          
          {!isDropOffDoorOpen ? (
            <form onSubmit={handleOpenDropOffCompartment} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Tracking Code</label>
                <input
                  type="text"
                  name="trackingCode"
                  value={dropOffCode}
                  onChange={(e) => setDropOffCode(e.target.value)}
                  placeholder="Tracking code"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
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
                  required
                />
              </div>
              <button
                type="submit"
                className="w-full py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors"
              >
                Open Compartment
              </button>
            </form>
          ) : (
            <div className="space-y-4 text-center">
              <div className="p-4 bg-amber-50 border border-amber-200 rounded-lg">
                <p className="text-amber-800 text-sm font-medium">
                  Compartment open for tracking code <span className="font-bold">{dropOffCode}</span>.
                </p>
              </div>
              <button
                type="button"
                onClick={handleCloseDropOffDoor}
                className="w-full py-3 bg-amber-600 hover:bg-amber-700 text-white font-medium rounded-lg transition-colors shadow-sm"
              >
                Close Door
              </button>
            </div>
          )}
        </div>

        {/* Pick Up Section */}
        <div className="p-6 bg-white border border-gray-200 rounded-lg shadow-sm space-y-4">
          <h2 className="text-xl font-semibold text-gray-800 border-b pb-2">Pick Up</h2>
          
          {!isPickUpDoorOpen ? (
            <form onSubmit={handleOpenPickUpCompartment} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Tracking Code</label>
                <input
                  type="text"
                  name="trackingCode"
                  value={pickUpTrackingCode}
                  onChange={(e) => setPickUpTrackingCode(e.target.value)}
                  placeholder="Tracking code"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500"
                  required
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
                  required
                />
              </div>
              <button
                type="submit"
                className="w-full py-2 bg-green-700 hover:bg-green-800 text-white font-medium rounded-lg transition-colors"
              >
                Open Compartment
              </button>
            </form>
          ) : (
            <div className="space-y-4 text-center">
              <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-lg">
                <p className="text-emerald-800 text-sm font-medium">
                  Compartment open for tracking code <span className="font-bold">{pickUpTrackingCode}</span>.
                </p>
              </div>
              <button
                type="button"
                onClick={handleClosePickUpDoor}
                className="w-full py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-medium rounded-lg transition-colors shadow-sm"
              >
                Close Door
              </button>
            </div>
          )}
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
