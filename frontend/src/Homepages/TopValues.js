/**
 * @file TopValues.js
 * @description 
 * This component displays a curated list of featured contests on the homepage.
 * It includes:
 *  - A section title and subtitle encouraging users to explore competitions.
 *  - A list of sample contest cards rendered using the ContestCard component.
 * 
 * Each card displays contest details such as title, description, image, organizer, date, category, and vote count.
 * Sample data is used to populate the cards for demonstration purposes.
 * Layout and styling are managed via TopValues.css.
 * 
 * Developer: Beiqi Dai
 */


// TopValues.js
import React from 'react';
import './TopValues.css';
import ContestCard from './ContestCard';

function TopValues() {
  // Three sample card data are defined here
  const sampleValues = [
    {
      id: 1,
      title: "Car Design Contest",
      organizer: "TechCarLead",
      date: "2025.04.10 ~ 05.10",
      description: "Build an amazing car design!",
      category: "Design",
      votes: 12,
      image:
        "https://i.pinimg.com/236x/c1/45/d8/c145d86ee6d5542020ca41c36113687d.jpg",
    },
    {
      id: 2,
      title: "Music Contest",
      organizer: "Music Master",
      date: "2025.05.01 ~ 06.01",
      description: "Create an innovative music!",
      category: "Music",
      votes: 34,
      image:
        "https://i.pinimg.com/474x/34/13/b2/3413b2fa409dc396c12527fc349eea70.jpg",
    },
    {
      id: 3,
      title: "Cocktail Challenge",
      organizer: "Wine Hub",
      date: "2025.06.15 ~ 07.15",
      description: "Showcase your cocktail skills!",
      category: "Food",
      votes: 56,
      image:
        "https://i.pinimg.com/236x/6a/b7/c2/6ab7c2657078ee3dc1dd1aaaaf7316b2.jpg",
    },
    {
      id: 4,
    title: "Story Writing Challenge",
    organizer: "Literature Hub",
    date: "2025.06.15 ~ 2025.07.15",
    description: "Craft a compelling short story!",
    category: "Literature",
    votes: 56,
    image: "https://i.pinimg.com/236x/43/0d/e5/430de57a137f68ab0c3bb2ecce4cdbcb.jpg",
  }
  ];

  return (
    <section className="top-values">
      <h2 className="top-values__title">Discover Top Opportunities</h2>
      <p className="top-values__subtitle">
      Explore a world of competitions and unlock your potential
      </p>

      <div className="top-values__list">
        {sampleValues.map((item, index) => {
          // Combine the fields required by the ContestCard 
          // If your ContestCard accepts {contest}, it is constructed as follows:
          const contest = {
            id: index,            // if you have a unique ID, use it here
            title: item.title,
            description: item.description,
            image: item.image,
            organizer: 'Default Organizer', // if unnecessary, you can remove this line
            date: '2025-01-01 ~ 2025-01-31',
            votes: 0,
            category: 'General',
          };
          
          return (
            <ContestCard
              key={index}
              contest={contest}
              // If you need callbacks such as onCardClick or onVote, you can pass them in here
            />
          );
        })}
      </div>
    </section>
  );
}

export default TopValues;
