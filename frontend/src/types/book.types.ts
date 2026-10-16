export interface Book {
  id: string;
  bookName: string;
  author: string | null;
  publisher: string | null;
  category: string | null;
  boughtAt: string | null;
  read: boolean;
  comments: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Library {
  id: string;
  name: string;
  createdAt: string;
}

export interface BookRequest {
  libraryId?: string;
  bookName: string;
  author?: string;
  publisher?: string;
  category?: string;
  boughtAt?: string;
  read: boolean;
  comments?: string;
}

export interface BookImportResult {
  imported: number;
  updated: number;
  skipped: number;
  errors: string[];
  columns: Record<string, string>;
  unmatchedHeaders: string[];
}

export interface BookFilters {
  search?: string;
  category?: string;
  isRead?: boolean;
}
