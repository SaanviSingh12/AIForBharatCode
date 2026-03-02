import React from 'react';
import { useNavigate, useParams } from 'react-router';
import { ArrowLeft, Phone, MapPin, Building, Award, Calendar, Clock, Languages } from 'lucide-react';
import { mockDoctors } from '../data/mockData';
import { useTranslation } from '../i18n';
import { Card } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Badge } from '../components/ui/badge';

export const DoctorDetails: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const t = useTranslation();
  const doctor = mockDoctors.find(d => d.id === id);

  if (!doctor) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>{t.doctor_not_found}</p>
      </div>
    );
  }

  const getDoctorTypeColor = (type: typeof doctor.type) => {
    switch (type) {
      case 'government':
        return 'bg-green-100 text-green-800 border-green-300';
      case 'independent':
        return 'bg-blue-100 text-blue-800 border-blue-300';
      case 'commercial':
        return 'bg-purple-100 text-purple-800 border-purple-300';
    }
  };

  const handleCall = () => {
    window.location.href = `tel:${doctor.phone}`;
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-green-50">
      {/* Header */}
      <div className="bg-gradient-to-r from-blue-600 to-green-600 text-white p-4 rounded-b-3xl">
        <Button
          variant="ghost"
          size="icon"
          onClick={() => navigate('/doctor-search')}
          className="text-white hover:bg-white/20 mb-4"
        >
          <ArrowLeft className="w-5 h-5" />
        </Button>

        <div className="flex items-start gap-4">
          <div className="w-20 h-20 bg-white/20 rounded-2xl flex items-center justify-center text-4xl">
            👨‍⚕️
          </div>
          <div className="flex-1">
            <h1 className="font-bold text-xl mb-1">{doctor.name}</h1>
            <p className="text-blue-100 mb-2">{doctor.specialty}</p>
            <div className="flex items-center gap-2 text-sm">
              <MapPin className="w-4 h-4" />
              <span>{doctor.distance} {t.doctor_away}</span>
            </div>
          </div>
        </div>
      </div>

      <div className="p-6 space-y-4">
        {/* Type Badge */}
        <div className="flex justify-center">
          <Badge className={`${getDoctorTypeColor(doctor.type)} text-sm px-4 py-2`}>
            {doctor.type === 'government' && t.doctor_govt_badge}
            {doctor.type === 'independent' && t.doctor_independent_badge}
            {doctor.type === 'commercial' && t.doctor_commercial_badge}
          </Badge>
        </div>

        {/* Details Card */}
        <Card className="p-6 space-y-4">
          <div className="flex items-start gap-3">
            <Building className="w-5 h-5 text-gray-600 mt-1" />
            <div>
              <p className="text-sm text-gray-600 mb-1">{t.doctor_address}</p>
              <p className="text-gray-900">{doctor.address}</p>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <Award className="w-5 h-5 text-gray-600 mt-1" />
            <div>
              <p className="text-sm text-gray-600 mb-1">Experience</p>
              <p className="text-gray-900">{doctor.experience} {t.years}</p>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <Languages className="w-5 h-5 text-gray-600 mt-1" />
            <div>
              <p className="text-sm text-gray-600 mb-2">{t.doctor_languages_spoken}</p>
              <div className="flex flex-wrap gap-2">
                {doctor.languages.map((lang, idx) => (
                  <span
                    key={idx}
                    className="bg-gray-100 text-gray-700 px-3 py-1 rounded-full text-sm"
                  >
                    {lang}
                  </span>
                ))}
              </div>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <Phone className="w-5 h-5 text-gray-600 mt-1" />
            <div>
              <p className="text-sm text-gray-600 mb-1">{t.doctor_contact}</p>
              <p className="text-gray-900 font-semibold">{doctor.phone}</p>
            </div>
          </div>
        </Card>

        {/* Timings Card (Mock) */}
        <Card className="p-6">
          <div className="flex items-center gap-2 mb-4">
            <Clock className="w-5 h-5 text-gray-600" />
            <h3 className="font-semibold text-gray-900">{t.doctor_consultation_hours}</h3>
          </div>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">{t.doctor_mon_fri}</span>
              <span className="text-gray-900 font-semibold">9:00 AM - 5:00 PM</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">{t.doctor_saturday}</span>
              <span className="text-gray-900 font-semibold">9:00 AM - 1:00 PM</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">{t.doctor_sunday}</span>
              <span className="text-red-600 font-semibold">{t.doctor_closed}</span>
            </div>
          </div>
        </Card>

        {/* PM-JAY Info */}
        {doctor.type === 'government' && (
          <Card className="p-4 bg-orange-50 border-orange-200">
            <div className="flex items-start gap-3">
              <div className="w-10 h-10 bg-orange-500 rounded-full flex items-center justify-center flex-shrink-0">
                <Award className="w-5 h-5 text-white" />
              </div>
              <div>
                <h3 className="font-semibold text-orange-900 mb-1">{t.doctor_pmjay_empanelled}</h3>
                <p className="text-sm text-orange-800">
                  {t.doctor_pmjay_free_desc}
                </p>
              </div>
            </div>
          </Card>
        )}

        {/* Action Buttons */}
        <div className="space-y-3 pt-4">
          <Button
            onClick={handleCall}
            className="w-full bg-green-600 hover:bg-green-700 h-12 text-base"
          >
            <Phone className="w-5 h-5 mr-2" />
            {t.doctor_call}
          </Button>
          <Button
            variant="outline"
            className="w-full h-12 text-base"
            onClick={() => {
              // Mock booking functionality
              alert(t.doctor_booking_msg);
            }}
          >
            <Calendar className="w-5 h-5 mr-2" />
            {t.doctor_book_appointment}
          </Button>
        </div>
      </div>
    </div>
  );
};
