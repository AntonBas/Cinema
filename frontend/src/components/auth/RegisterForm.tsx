import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import './RegisterForm.css';

export const RegisterForm: React.FC = () => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    dateOfBirth: '',
    city: '',
    email: '',
    phoneNumber: '',
    password: '',
    passwordConfirm: ''
  });
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const { register } = useAuth();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    if (formData.password !== formData.passwordConfirm) {
      setError('Passwords do not match');
      setIsLoading(false);
      return;
    }

    try {
      await register(formData);
      alert('Registration successful! Please check your email to confirm your account.');
      window.location.href = '/login';
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <section className="registration">
      <h1 className="registration-title">Registration</h1>
      <div className="registration-container">
        <form className="registration-form" onSubmit={handleSubmit}>
          {error && <div className="error-message">{error}</div>}
          
          <div className="registration-top">
            <div className="registration-group-first">
              <h2 className="registration-text">Personal Information</h2>
              <input
                placeholder="First Name"
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                className="form-control"
                required
              />
              <input
                placeholder="Last Name"
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                className="form-control"
                required
              />
            </div>
            <div className="registration-group-second">
              <input
                type="date"
                name="dateOfBirth"
                value={formData.dateOfBirth}
                onChange={handleChange}
                className="form-control"
                required
              />
              <input
                placeholder="Your City"
                type="text"
                name="city"
                value={formData.city}
                onChange={handleChange}
                className="form-control"
                required
              />
            </div>
          </div>
          
          <div className="registration-middle">
            <h2 className="registration-text">Contact Information</h2>
            <input
              placeholder="E-mail"
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              className="form-control"
              required
            />
            <input
              placeholder="Phone number"
              type="text"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
              className="form-control"
              required
            />
          </div>
          
          <div className="registration-bottom">
            <h2 className="registration-text">Create a Password</h2>
            <input
              placeholder="Enter Password"
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              className="form-control"
              required
            />
            <input
              placeholder="Confirm Password"
              type="password"
              name="passwordConfirm"
              value={formData.passwordConfirm}
              onChange={handleChange}
              className="form-control"
              required
            />
          </div>
          
          <button 
            type="submit" 
            className="registration-confirm registration-text"
            disabled={isLoading}
          >
            {isLoading ? 'Creating Account...' : 'Sign Up'}
          </button>
        </form>
      </div>
    </section>
  );
};