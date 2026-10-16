import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface LibraryState {
  currentLibraryId: string | null;
  setCurrentLibraryId: (id: string | null) => void;
}

// The library the user is working in. Persisted so reloads stay in the same library.
export const useLibraryStore = create<LibraryState>()(
  persist(
    (set) => ({
      currentLibraryId: null,
      setCurrentLibraryId: (id) => set({ currentLibraryId: id }),
    }),
    { name: 'library-selection' }
  )
);
