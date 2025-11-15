"use client";

import { Plus } from "lucide-react";
import { ShoppingCart } from "lucide-react";
import { Button } from "@/components/ui/button";
import { StationDialog } from "./StationDialog";
import { User } from "./types";

interface StationManagementHeaderProps {
  isAdmin: boolean;
  isCreateDialogOpen: boolean;
  onCreateDialogOpenChange: (open: boolean) => void;
  allUsers: User[];
  userFetchError: string | null;
  onCreate: (data: {
    name: string;
    description: string;
    userIds: string[];
  }) => Promise<void>;
}

export function StationManagementHeader({
  isAdmin,
  isCreateDialogOpen,
  onCreateDialogOpenChange,
  allUsers,
  userFetchError,
  onCreate,
}: StationManagementHeaderProps) {
  return (
    <div className="rounded-lg bg-card p-6 shadow-sm mb-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <ShoppingCart className="w-8 h-8 text-blue-600" />
          <div>
            <h1 className="text-3xl font-bold text-gray-100">
              {isAdmin ? "POS Station Management" : "Select POS Station"}
            </h1>
            <p className="text-sm text-gray-400 mt-1">
              {isAdmin
                ? "Manage your bar stations and assign users"
                : "Choose a station to start selling"}
            </p>
          </div>
        </div>

        {isAdmin && (
          <StationDialog
            mode="create"
            users={allUsers}
            userFetchError={userFetchError}
            isOpen={isCreateDialogOpen}
            onOpenChange={onCreateDialogOpenChange}
            onSubmit={onCreate}
            trigger={
              <Button className="bg-blue-600 hover:bg-blue-700">
                <Plus className="w-4 h-4 mr-2" />
                Create Station
              </Button>
            }
          />
        )}
      </div>
    </div>
  );
}

