import React from 'react';
import './Tours.css';

function Tours() {
  const tours = [
    {
      title: 'Alone with nature',
      price: '$100 / person',
      image: 'https://via.placeholder.com/200',
    },
    {
      title: 'Jeep ride',
      price: '$200 / person',
      image: 'https://via.placeholder.com/200',
    },
    {
      title: 'Hiking tour',
      price: '$150 / person',
      image: 'https://via.placeholder.com/200',
    },
    {
      title: 'Corners of the island',
      price: '$80 / person',
      image: 'https://via.placeholder.com/200',
    },
  ];

  return (
    <section className="tours">
      <h2 className="tours__title">Choose your contest</h2>
      <div className="tours__cards">
        {tours.map((tour, index) => (
          <div className="tour-card" key={index}>
            <img src={tour.image} alt={tour.title} />
            <h3>{tour.title}</h3>
            <p>{tour.price}</p>
          </div>
        ))}
      </div>
      <button className="tours__button">See all</button>
    </section>
  );
}

export default Tours;
