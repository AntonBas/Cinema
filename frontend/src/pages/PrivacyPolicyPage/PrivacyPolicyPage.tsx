import React from "react";
import { Layout } from "@/components/layout/Layout/Layout";
import { PageContainer } from "@/components/ui/PageContainer/PageContainer";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import styles from "./PrivacyPolicyPage.module.css";

const CONTACT_EMAIL = "antonbas21@gmail.com";

interface PolicySection {
  title: string;
  items: string[];
}

const SECTIONS: PolicySection[] = [
  {
    title: "What we collect",
    items: [
      "Account details: email, first and last name, date of birth, city and phone number.",
      "If you sign in with Google: your email address and name from your Google profile. We do not get access to your Google password, contacts or any other Google data.",
      "Bookings, tickets, payments, refunds and bonus point history linked to your account.",
      "For card payments: the payment status and a masked card number returned by LiqPay. Full card details are entered on LiqPay's page and never reach our servers.",
    ],
  },
  {
    title: "How we use it",
    items: [
      "To create and secure your account and keep you signed in.",
      "To reserve seats, process payments and refunds, and issue tickets with QR codes.",
      "To send service emails: account verification, password reset, tickets and refund confirmations.",
      "To run the bonus program, including the birthday bonus.",
    ],
  },
  {
    title: "Cookies",
    items: [
      "We use a single httpOnly authentication cookie to keep you signed in. We do not use advertising or tracking cookies.",
    ],
  },
  {
    title: "Who processes your data",
    items: [
      "LiqPay processes payments. Brevo delivers emails.",
      "The application is hosted on Render and Vercel, with data stored on Neon (PostgreSQL), Upstash (Redis) and Cloudinary (movie images).",
      "We do not sell your data or share it for advertising.",
    ],
  },
  {
    title: "Your choices",
    items: [
      "You can view and update your profile in your account at any time.",
      `To delete your account and personal data, email ${CONTACT_EMAIL}. Payment records may be kept where needed for refunds.`,
    ],
  },
];

export const PrivacyPolicyPage: React.FC = () => (
  <Layout>
    <PageContainer size="narrow">
      <PageHeader
        title="Privacy Policy"
        subtitle="What data Bas Cinema collects and how it is used"
      />

      <div className={styles.sections}>
        {SECTIONS.map((section) => (
          <section key={section.title} className={styles.section}>
            <h3>{section.title}</h3>
            <ul>
              {section.items.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </section>
        ))}

        <section className={styles.section}>
          <h3>Contact</h3>
          <p>
            Questions about your data:{" "}
            <a href={`mailto:${CONTACT_EMAIL}`}>{CONTACT_EMAIL}</a>
          </p>
        </section>
      </div>
    </PageContainer>
  </Layout>
);
