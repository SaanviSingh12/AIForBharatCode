import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { getTranslations } from "../../i18n";
import { useApp } from "../context/AppContext";
import { getDoctors, type DoctorDto, type HospitalDto } from "../services/api";
import HospitalProfile from "./HospitalProfile";

const DoctorProfile: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { triageResult, language } = useApp();
    const t = getTranslations(language);
    const [hospital, setHospital] = useState<HospitalDto | null>(null);
    const [doctor, setDoctor] = useState<DoctorDto | null>(null);

    useEffect(() => {
        // Try to find hospital from triageResult
        if (triageResult?.hospitals) {
            const foundHospital = triageResult.hospitals.find((h: HospitalDto) => h.id === id);
            if (foundHospital && foundHospital.type === "government") {
                setHospital(foundHospital);
                setDoctor(null);
                return;
            }
        }
        // Otherwise, fetch doctor from API
        getDoctors({}, {}).then((docs: DoctorDto[]) => {
            const foundDoctor = docs.find((d) => d.id === id);
            if (foundDoctor) {
                setDoctor(foundDoctor);
                setHospital(null);
            }
        });
    }, [id, triageResult]);

    if (hospital) {
        return <HospitalProfile hospital={hospital} />;
    }

    if (doctor) {
        return (
            <div className="max-w-lg mx-auto mt-10 bg-white rounded-2xl shadow-xl p-8 flex flex-col gap-6">
                <h2 className="text-3xl font-bold mb-2 leading-tight">{doctor.name}</h2>
                <div className="text-blue-600 text-lg font-semibold mb-2">
                    Specialist: {doctor.specialty}
                </div>
                <div className="text-gray-700 text-base mb-1">
                    Address: {doctor.address}
                </div>
                <div className="text-gray-700 text-base mb-1">
                    Phone: {doctor.phone}
                </div>
                <div className="flex gap-3 mb-2">
                    <div className="bg-gray-100 px-4 py-1 rounded-lg text-sm font-medium text-gray-700">
                        {doctor.fee === 0 ? "Free" : doctor.fee ? `Paid` : "Unknown"}
                    </div>
                    {doctor.fee > 0 && (
                        <div className="bg-gray-100 px-4 py-1 rounded-lg text-sm font-medium text-gray-700">
                            Fee: ₹{doctor.fee}
                        </div>
                    )}
                </div>
                <button
                    className="w-full bg-blue-600 hover:bg-blue-700 transition-colors text-white py-3 rounded-xl font-semibold text-lg flex items-center justify-center gap-2 shadow-md mb-2"
                    onClick={() => window.open(`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(doctor.address ?? "")}`, "_blank")}
                >
                    <span role="img" aria-label="map">🗺️</span> {t.directions}
                </button>
                <button
                    className="w-full bg-green-600 hover:bg-green-700 transition-colors text-white py-3 rounded-xl font-semibold text-lg flex items-center justify-center gap-2 shadow-md mb-2"
                    onClick={() => window.location.href = `tel:${doctor.phone}`}
                >
                    <span role="img" aria-label="phone">📞</span> Call
                </button>
            </div>
        );
    }

    return (
        <div className="p-8 text-center text-gray-500">Doctor not found.</div>
    );
};

export default DoctorProfile;
