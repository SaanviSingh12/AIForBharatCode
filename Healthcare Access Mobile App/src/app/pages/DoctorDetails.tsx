import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import { ArrowLeft, Phone, MapPin, Building, Award, Calendar, Clock, Languages } from 'lucide-react';
import { Doctor, mockDoctors } from '../data/mockData';
import { Card } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Badge } from '../components/ui/badge';
import { Skeleton } from '../components/ui/skeleton';
import { getDoctorById, type DoctorDto } from '../services/api';

export const DoctorDetails: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams();

  const [doctor, setDoctor] = useState<Doctor | null>(
    mockDoctors.find(d => d.id === id) || null
  );
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    const fetchDoctor = async () => {
      setIsLoading(true);
      try {
        const data: DoctorDto = await getDoctorById(id);
        if (!cancelled) {
          setDoctor({
            id: data.id,
            name: data.name,
            specialty: data.specialty,
            type: data.type as Doctor['type'],
            distance: data.distance,
            phone: data.phone,
            address: data.address,
            experience: data.experience,
            languages: data.languages || [],
          });
        }
      } catch {
        // Keep mock data fallback
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    };
    fetchDoctor();
    return () => { cancelled = true; };
  }, [id]);

  if (isLoading && !doctor) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-green-50 p-6 space-y-4">
        <Skeleton className="h-40 w-full rounded-b-3xl" />
        <Skeleton className="h-10 w-1/2 mx-auto" />
        <Skeleton className="h-48 w-full" />
        <Skeleton className="h-32 w-full" />
      </div>
    );
  }

  if (!doctor) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>Doctor not found</p>
      </div>
    );
  }

  const getDoctorTypeColor = (type: Doctor['type']) => {
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
              <span>{doctor.distance} km away</span>
            </div>
          </div>
        </div>
      </div>

      <div className="p-6 space-y-4">
        {/* Type Badge */}
        <div className="flex justify-center">
          <Badge className={`${getDoctorTypeColor(doctor.type)} text-sm px-4 py-2`}>
            {doctor.type === 'government' && '🏛️ Government Doctor'}
            {doctor.type === 'independent' && '🏥 Independent Practice'}
            {doctor.type === 'commercial' && '🏨 Commercial Hospital'}
          </Badge>
        </div>

        {/* Details Card */}
        <Card className="p-6 space-y-4">
          <div className="flex items-start gap-3">
            <Building className="w-5 h-5 text-gray-600 mt-1" />
            <div>
              <p className="text-sm text-gray-600 mb-1">Address</p>
              <p className="text-gray-900">{doctor.address}</p>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <Award className="w-5 h-5 text-gray-600 mt-1" />
            <div>
              <p className="text-sm text-gray-600 mb-1">Experience</p>
              <p className="text-gray-900">{doctor.experience} years</p>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <Languages className="w-5 h-5 text-gray-600 mt-1" />
            <div>
              <p className="text-sm text-gray-600 mb-2">Languages Spoken</p>
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
              <p className="text-sm text-gray-600 mb-1">Contact</p>
              <p className="text-gray-900 font-semibold">{doctor.phone}</p>
            </div>
          </div>
        </Card>

        {/* Timings Card (Mock) */}
        <Card className="p-6">
          <div className="flex items-center gap-2 mb-4">
            <Clock className="w-5 h-5 text-gray-600" />
            <h3 className="font-semibold text-gray-900">Consultation Hours</h3>
          </div>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">Monday - Friday</span>
              <span className="text-gray-900 font-semibold">9:00 AM - 5:00 PM</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Saturday</span>
              <span className="text-gray-900 font-semibold">9:00 AM - 1:00 PM</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Sunday</span>
              <span className="text-red-600 font-semibold">Closed</span>
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
                <h3 className="font-semibold text-orange-900 mb-1">PM-JAY Empanelled</h3>
                <p className="text-sm text-orange-800">
                  Free consultation and treatment available under Ayushman Bharat scheme
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
            Call Doctor
          </Button>
          <Button
            variant="outline"
            className="w-full h-12 text-base"
            onClick={() => {
              // Mock booking functionality
              alert('Booking functionality will be implemented with backend');
            }}
          >
            <Calendar className="w-5 h-5 mr-2" />
            Book Appointment
          </Button>
        </div>
      </div>
    </div>
  );
};
