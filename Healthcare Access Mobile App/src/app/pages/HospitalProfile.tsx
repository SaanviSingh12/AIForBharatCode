import React from "react";

interface HospitalProfileProps {
  hospital: {
    name: string;
    address?: string;
    specialist?: string;
    phone?: string;
    type?: string;
    [key: string]: any;
  };
}

const HospitalProfile: React.FC<HospitalProfileProps> = ({ hospital }) => {
  return (
    <div className="max-w-lg mx-auto mt-10 bg-white rounded-2xl shadow-xl p-8 flex flex-col gap-6">
      <button className="mb-2 text-gray-500 text-sm font-medium hover:underline flex items-center" onClick={() => window.history.back()}>
        <span className="mr-2">←</span> Find Doctor
      </button>
      <div>
        <h2 className="text-3xl font-bold mb-2 leading-tight">{hospital.name}</h2>
        {hospital.specialist && (
          <div className="text-blue-600 text-lg font-semibold mb-4">{hospital.specialist}</div>
        )}
        <div className="flex gap-3 mb-5">
          <div className="bg-gray-100 px-4 py-1 rounded-lg text-sm font-medium text-gray-700">{hospital.type}</div>
          <div className="bg-gray-100 px-4 py-1 rounded-lg text-sm font-medium text-gray-700">{hospital.distance} km</div>
          <div className="bg-gray-100 px-4 py-1 rounded-lg text-sm font-medium text-gray-700">{hospital.hasEmergency ? "Emergency Available" : "No Emergency"}</div>
        </div>
      </div>
      <div className="bg-gray-50 rounded-xl p-5 text-sm flex flex-col gap-2 border border-gray-200">
        <div className="flex justify-between items-center">
          <span className="text-gray-600">Fee</span>
          <span className="font-semibold text-gray-900">{hospital.free ? "Free" : `₹${hospital.fee}`}</span>
        </div>
        <div className="flex justify-between items-center">
          <span className="text-gray-600">PMJAY</span>
          <span className="font-semibold text-gray-900">{hospital.pmjayStatus === "empanelled" ? "Empanelled" : "Not Empanelled"}</span>
        </div>
        <div className="flex justify-between items-center">
          <span className="text-gray-600">Phone</span>
          <span className="font-semibold text-gray-900">{hospital.phone}</span>
        </div>
        <div className="flex justify-between items-center">
          <span className="text-gray-600">Address</span>
          <span className="font-semibold text-gray-900">{hospital.address}</span>
        </div>
        <div className="flex justify-between items-center">
          <span className="text-gray-600">Latitude</span>
          <span className="font-semibold text-gray-900">{hospital.latitude}</span>
        </div>
        <div className="flex justify-between items-center">
          <span className="text-gray-600">Longitude</span>
          <span className="font-semibold text-gray-900">{hospital.longitude}</span>
        </div>
      </div>
      <button
        className="w-full bg-blue-600 hover:bg-blue-700 transition-colors text-white py-3 rounded-xl font-semibold text-lg flex items-center justify-center gap-2 shadow-md mb-2"
        onClick={() => window.open(`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(hospital.address ?? "")}`, "_blank")}
      >
        <span role="img" aria-label="map">🗺️</span> Open in Google Maps
      </button>
      <button
        className="w-full border border-gray-300 py-3 rounded-xl flex items-center justify-center text-blue-700 font-semibold text-lg gap-2 hover:bg-blue-50 transition-colors"
        onClick={() => window.location.href = `tel:${hospital.phone}`}
      >
        <span role="img" aria-label="phone">📞</span> Call {hospital.phone}
      </button>
    </div>
  );
};

export default HospitalProfile;
