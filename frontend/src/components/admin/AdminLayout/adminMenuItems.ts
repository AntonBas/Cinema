import type { LucideIcon } from "lucide-react";
import {
  Film,
  Calendar,
  Building2,
  Users,
  Gift,
  Tag,
  Ticket,
  ScrollText,
  ReceiptText,
} from "lucide-react";

export interface AdminMenuItem {
  path: string;
  label: string;
  icon: LucideIcon;
  roles: string[];
}

const ADMIN_MENU_ITEMS: AdminMenuItem[] = [
  {
    path: "/admin/movies",
    label: "Movies",
    icon: Film,
    roles: ["ROLE_ADMIN", "ROLE_CONTENT_MANAGER"],
  },
  {
    path: "/admin/schedule",
    label: "Schedule",
    icon: Calendar,
    roles: ["ROLE_ADMIN", "ROLE_CONTENT_MANAGER"],
  },
  {
    path: "/admin/halls",
    label: "Halls",
    icon: Building2,
    roles: ["ROLE_ADMIN", "ROLE_CONTENT_MANAGER"],
  },
  {
    path: "/admin/users",
    label: "Users",
    icon: Users,
    roles: ["ROLE_ADMIN", "ROLE_CASHIER"],
  },
  {
    path: "/admin/bookings",
    label: "Bookings",
    icon: ReceiptText,
    roles: ["ROLE_ADMIN", "ROLE_CASHIER"],
  },
  {
    path: "/admin/bonus",
    label: "Bonus Rules",
    icon: Gift,
    roles: ["ROLE_ADMIN"],
  },
  {
    path: "/admin/promotion",
    label: "Promotions",
    icon: Tag,
    roles: ["ROLE_ADMIN", "ROLE_CONTENT_MANAGER"],
  },
  {
    path: "/admin/ticket-type",
    label: "Ticket Types",
    icon: Ticket,
    roles: ["ROLE_ADMIN"],
  },
  {
    path: "/admin/audit-logs",
    label: "Audit Logs",
    icon: ScrollText,
    roles: ["ROLE_ADMIN"],
  },
];

export const getAdminMenuItems = (userRole: string): AdminMenuItem[] =>
  ADMIN_MENU_ITEMS.filter((item) => item.roles.includes(userRole));
