export const BOOKING_STEPS = [
    {
        id: 1,
        title: 'Select Seats',
        description: 'Choose your seats',
        path: '/booking/:sessionId',
        isClickable: true
    },
    {
        id: 2,
        title: 'Booking Summary',
        description: 'Review your booking',
        path: '/booking/summary/:bookingId',
        isClickable: true
    },
    {
        id: 3,
        title: 'Payment',
        description: 'Secure payment',
        path: '/booking/payment/:bookingId',
        isClickable: false
    },
    {
        id: 4,
        title: 'Confirmation',
        description: 'Booking confirmed',
        path: '/booking/success',
        isClickable: false
    }
];