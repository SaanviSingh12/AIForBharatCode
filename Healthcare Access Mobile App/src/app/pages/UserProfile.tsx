import React, { useState } from 'react';
import { useNavigate } from 'react-router';
import { ArrowLeft, User, Calendar, Users, Phone, MapPin, Save, Edit, Heart, Award } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { useTranslation } from '../i18n';
import { Card } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { BottomNav } from '../components/BottomNav';

export const UserProfile: React.FC = () => {
  const navigate = useNavigate();
  const { userProfile, setUserProfile } = useApp();
  const t = useTranslation();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState(userProfile);

  const handleSave = () => {
    setUserProfile(formData);
    setIsEditing(false);
  };

  const handleChange = (field: string, value: string) => {
    setFormData({ ...formData, [field]: value });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-green-50 pb-24">
      {/* Header */}
      <div className="bg-gradient-to-r from-blue-600 to-green-600 text-white p-4 rounded-b-3xl">
        <div className="flex items-center justify-between mb-6">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => navigate('/home')}
            className="text-white hover:bg-white/20"
          >
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <h1 className="font-semibold text-lg">{t.profile_title}</h1>
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setIsEditing(!isEditing)}
            className="text-white hover:bg-white/20"
          >
            <Edit className="w-5 h-5" />
          </Button>
        </div>

        <div className="flex flex-col items-center">
          <div className="w-24 h-24 bg-white/20 rounded-full flex items-center justify-center text-5xl mb-3">
            👤
          </div>
          <h2 className="font-bold text-xl">{userProfile.name}</h2>
          <p className="text-blue-100">{userProfile.age} years • {userProfile.gender}</p>
        </div>
      </div>

      <div className="p-6 space-y-4">
        {/* Personal Information */}
        <Card className="p-6">
          <h3 className="font-semibold text-gray-900 mb-4">{t.profile_personal_info}</h3>
          
          {isEditing ? (
            <div className="space-y-4">
              <div>
                <Label htmlFor="name">{t.profile_full_name}</Label>
                <Input
                  id="name"
                  value={formData.name}
                  onChange={(e) => handleChange('name', e.target.value)}
                  className="mt-1"
                />
              </div>
              <div>
                <Label htmlFor="age">{t.profile_age}</Label>
                <Input
                  id="age"
                  value={formData.age}
                  onChange={(e) => handleChange('age', e.target.value)}
                  className="mt-1"
                />
              </div>
              <div>
                <Label htmlFor="gender">{t.profile_gender}</Label>
                <Input
                  id="gender"
                  value={formData.gender}
                  onChange={(e) => handleChange('gender', e.target.value)}
                  className="mt-1"
                />
              </div>
              <Button
                onClick={handleSave}
                className="w-full bg-green-600 hover:bg-green-700"
              >
                <Save className="w-4 h-4 mr-2" />
                {t.profile_save_changes}
              </Button>
            </div>
          ) : (
            <div className="space-y-3">
              <div className="flex items-center gap-3">
                <User className="w-5 h-5 text-gray-600" />
                <div>
                  <p className="text-sm text-gray-600">{t.profile_name_label}</p>
                  <p className="text-gray-900 font-semibold">{userProfile.name}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Calendar className="w-5 h-5 text-gray-600" />
                <div>
                  <p className="text-sm text-gray-600">{t.profile_age}</p>
                  <p className="text-gray-900 font-semibold">{userProfile.age} {t.years}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Users className="w-5 h-5 text-gray-600" />
                <div>
                  <p className="text-sm text-gray-600">{t.profile_gender}</p>
                  <p className="text-gray-900 font-semibold">{userProfile.gender}</p>
                </div>
              </div>
            </div>
          )}
        </Card>

        {/* Contact Information (Mock) */}
        <Card className="p-6">
          <h3 className="font-semibold text-gray-900 mb-4">{t.profile_contact_info}</h3>
          <div className="space-y-3">
            <div className="flex items-center gap-3">
              <Phone className="w-5 h-5 text-gray-600" />
              <div>
                <p className="text-sm text-gray-600">{t.profile_phone}</p>
                <p className="text-gray-900 font-semibold">+91 98765 43210</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <MapPin className="w-5 h-5 text-gray-600" />
              <div>
                <p className="text-sm text-gray-600">{t.profile_location}</p>
                <p className="text-gray-900 font-semibold">New Delhi, India</p>
              </div>
            </div>
          </div>
        </Card>

        {/* Health Insurance */}
        <Card className="p-6 bg-orange-50 border-orange-200">
          <div className="flex items-start gap-3">
            <div className="w-12 h-12 bg-orange-500 rounded-xl flex items-center justify-center">
              <Award className="w-6 h-6 text-white" />
            </div>
            <div className="flex-1">
              <h3 className="font-semibold text-orange-900 mb-1">{t.profile_pmjay_status}</h3>
              <p className="text-sm text-orange-800 mb-3">
                {t.profile_health_card}
              </p>
              <div className="bg-white rounded-lg p-3">
                <div className="flex justify-between mb-2">
                  <span className="text-sm text-gray-600">{t.profile_card_number}</span>
                  <span className="text-sm font-semibold text-gray-900">1234-5678-9012</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">{t.profile_coverage}</span>
                  <span className="text-sm font-semibold text-green-600">₹5,00,000</span>
                </div>
              </div>
            </div>
          </div>
        </Card>

        {/* Medical History (Mock) */}
        <Card className="p-6">
          <h3 className="font-semibold text-gray-900 mb-4">{t.profile_medical_history}</h3>
          <div className="space-y-3">
            <div className="flex items-start gap-3 bg-blue-50 rounded-lg p-3">
              <Heart className="w-5 h-5 text-blue-600 mt-0.5" />
              <div>
                <p className="text-sm font-semibold text-gray-900">{t.profile_diabetes}</p>
                <p className="text-xs text-gray-600">{t.profile_diabetes_date}</p>
              </div>
            </div>
            <div className="flex items-start gap-3 bg-blue-50 rounded-lg p-3">
              <Heart className="w-5 h-5 text-blue-600 mt-0.5" />
              <div>
                <p className="text-sm font-semibold text-gray-900">{t.profile_hypertension}</p>
                <p className="text-xs text-gray-600">{t.profile_hypertension_date}</p>
              </div>
            </div>
          </div>
        </Card>

        {/* App Settings */}
        <Card className="p-6">
          <h3 className="font-semibold text-gray-900 mb-4">{t.profile_app_settings}</h3>
          <Button
            variant="outline"
            className="w-full justify-start"
            onClick={() => navigate('/')}
          >
            {t.profile_change_language}
          </Button>
        </Card>
      </div>

      <BottomNav />
    </div>
  );
};